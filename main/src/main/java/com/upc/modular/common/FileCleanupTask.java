package com.upc.modular.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.mapper.TeachingMaterialsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils; // 1. 引入 Spring 工具类，用于递归删除目录

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FileCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(FileCleanupTask.class);

    private final TeachingMaterialsMapper teachingMaterialsMapper;

    public FileCleanupTask(TeachingMaterialsMapper teachingMaterialsMapper) {
        this.teachingMaterialsMapper = teachingMaterialsMapper;
    }
    /**
     * 定时任务，每天凌晨3点执行，用于清理无用的教学素材文件。
     * cron表达式: 秒 分 时 日 月 周
     * "0 0 3 * * ?" 表示每天的 3:00:00 执行
     */
    @Scheduled(cron = "0 0 3 * * ?") // <-- 启用这一行
//@Scheduled(cron = "0/30 * * * * ?") // 测试每30秒删除一次
    public void cleanupUnusedTeachingMaterials() {
        log.info("--- [定时任务] 开始执行无用教学素材文件清理 ---");
        Path rootDirectory = Paths.get("upload", "teaching_materials");
        if (!Files.exists(rootDirectory)) {
            log.warn("[定时任务] 清理终止：根目录 '{}' 不存在。", rootDirectory);
            return;
        }
        try {
            // 2. 从数据库获取所有有效的 "文件路径" 和 "目录路径"
            Set<String> validFilePaths = getValidFilePathsFromDatabase();
            Set<String> validDirectoryPaths = getValidDirectoryPathsFromDatabase();
            log.info("[定时任务] 数据库中共有 {} 个有效的文件记录和 {} 个有效的目录记录。", validFilePaths.size(), validDirectoryPaths.size());
            // --- 步骤 A: 优先清理孤立的目录（如图集） ---
            cleanupOrphanDirectories(rootDirectory, validDirectoryPaths);

            // --- 步骤 B: 再清理所有孤立的单个文件 ---
            cleanupOrphanFiles(rootDirectory, validFilePaths, validDirectoryPaths);

            log.info("--- [定时任务] 清理任务执行完毕 ---");

        } catch (Exception e) {
            log.error("--- [定时任务] 执行过程中发生严重错误 ---", e);
        }
    }

    /**
     * 扫描并清理孤立的目录。
     */
    private void cleanupOrphanDirectories(Path rootDirectory, Set<String> validDirectoryPaths) {
        log.info("[定时任务] 开始扫描孤立目录...");
        // 我们只关心 imageSet 目录，因为目前只有它以目录形式存储
        Path imageSetBaseDir = rootDirectory.resolve("imageSet");
        if (!Files.exists(imageSetBaseDir) || !Files.isDirectory(imageSetBaseDir)) {
            log.info("[定时任务] imageSet 目录不存在，跳过目录清理。");
            return;
        }

        try (Stream<Path> dateDirs = Files.list(imageSetBaseDir)) {
            dateDirs.filter(Files::isDirectory).forEach(dateDir -> {
                try (Stream<Path> imageSetDirs = Files.list(dateDir)) {
                    imageSetDirs.filter(Files::isDirectory).forEach(imageSetDir -> {
                        String normalizedDir = normalizePath(imageSetDir.toString());
                        if (!validDirectoryPaths.contains(normalizedDir)) {
                            try {
                                FileSystemUtils.deleteRecursively(imageSetDir);
                                log.info("[定时任务] 已删除孤立的图集目录: {}", normalizedDir);
                            } catch (IOException e) {
                                log.error("[定时任务] 删除图集目录失败: {}", normalizedDir, e);
                            }
                        }
                    });
                } catch (IOException e) {
                    log.error("[定时任务] 遍历图集子目录失败: {}", dateDir, e);
                }
            });
        } catch (IOException e) {
            log.error("[定时任务] 遍历图集日期目录失败: {}", imageSetBaseDir, e);
        }
    }

    /**
     * 扫描并清理孤立的单个文件。
     */
    private void cleanupOrphanFiles(Path rootDirectory, Set<String> validFilePaths, Set<String> validDirectoryPaths) {
        log.info("[定时任务] 开始扫描孤立文件...");
        long filesDeletedCount = 0;
        try (Stream<Path> allFilesStream = Files.walk(rootDirectory)) {
            List<Path> allFiles = allFilesStream.filter(Files::isRegularFile).collect(Collectors.toList());

            log.info("[定时任务] 共扫描到磁盘文件 {} 个。", allFiles.size());

            for (Path filePathOnDisk : allFiles) {
                String normalizedPathOnDisk = normalizePath(filePathOnDisk.toString());

                // 检查文件是否在受保护的文件列表或受保护的目录中
                boolean isProtected = validFilePaths.contains(normalizedPathOnDisk) ||
                        isInsideProtectedDirectory(normalizedPathOnDisk, validDirectoryPaths);

                if (!isProtected) {
                    try {
                        Files.delete(filePathOnDisk);
                        log.info("[定时任务] 已删除孤立文件: {}", normalizedPathOnDisk);
                        filesDeletedCount++;
                    } catch (IOException e) {
                        log.error("[定时任务] 删除文件失败: {}", normalizedPathOnDisk, e);
                    }
                }
            }
            log.info("[定时任务] 文件清理完成，共删除了 {} 个孤立文件。", filesDeletedCount);
        } catch (IOException e) {
            log.error("[定时任务] 遍历磁盘文件时发生错误。", e);
        }
    }

    /**
     * 从数据库中仅获取被引用的、类型为 "imageSet" 的目录路径。
     */
    private Set<String> getValidDirectoryPathsFromDatabase() {
        List<TeachingMaterials> imageSets = teachingMaterialsMapper.selectList(
                new LambdaQueryWrapper<TeachingMaterials>().eq(TeachingMaterials::getType, "imageSet")
        );
        Set<String> validPaths = new HashSet<>();
        for (TeachingMaterials material : imageSets) {
            if (isValidPath(material.getFilePath())) {
                validPaths.add(normalizePath(material.getFilePath()));
            }
        }
        return validPaths;
    }

    /**
     * 从数据库中获取所有被引用的、非 "imageSet" 类型的文件路径，以及所有的封面图和二维码图路径。
     */
    private Set<String> getValidFilePathsFromDatabase() {
        List<TeachingMaterials> allMaterials = teachingMaterialsMapper.selectList(null);
        Set<String> validPaths = new HashSet<>();
        for (TeachingMaterials material : allMaterials) {
            // 只处理非图集类型的 file_path
            if (!"imageSet".equals(material.getType()) && isValidPath(material.getFilePath())) {
                validPaths.add(normalizePath(material.getFilePath()));
            }
            // 封面和二维码总是文件，需要保护
            if (isValidPath(material.getCoverImagePath())) {
                validPaths.add(normalizePath(material.getCoverImagePath()));
            }
            if (isValidPath(material.getQrcodePath())) {
                validPaths.add(normalizePath(material.getQrcodePath()));
            }
        }
        return validPaths;
    }

    /**
     * 检查一个文件路径是否位于一个受保护的目录（如图集目录）内部。
     */
    private boolean isInsideProtectedDirectory(String filePath, Set<String> protectedDirectories) {
        for (String dirPath : protectedDirectories) {
            // 如果文件路径以某个受保护的目录路径开头，并紧跟一个斜杠，则认为它在目录内
            if (filePath.startsWith(dirPath + "/")) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidPath(String path) {
        return Objects.nonNull(path) && !path.trim().isEmpty();
    }

    private String normalizePath(String path) {
        return path.replace(File.separator, "/");
    }
}