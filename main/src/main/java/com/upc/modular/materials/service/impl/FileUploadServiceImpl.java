package com.upc.modular.materials.service.impl;

import com.upc.common.utils.FileManageUtil;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.service.IFileUploadService;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class FileUploadServiceImpl implements IFileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadServiceImpl.class);
    private final String basePath = "upload";
    private static final List<String> ARCHIVE_EXTENSIONS = Arrays.asList(".zip", ".tar", ".gz", ".tgz", ".7z", ".jar");

    @Override
    public String uploadMaterialFile(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "上传文件不能为空");
        }

        if (!TeachingMaterials.SUPPORTED_TYPES.contains(type)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "不支持该素材类型: " + type);
        }

        Path folderPath = Paths.get(basePath, "teaching_materials", type, FileManageUtil.yyyyMMddStr());
        String finalPath;

        try {
            if ("simulation".equalsIgnoreCase(type) || "H5".equalsIgnoreCase(type)) {
                if (!isSupportedArchive(file)) {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "该类型必须上传支持的压缩包(zip, tar, 7z等)");
                }

                // 1. 保存原始压缩包
                String originalFileName = FileManageUtil.createFileName(file);
                String savedArchivePath = FileManageUtil.uploadFile(file, folderPath, originalFileName);

                if (ObjectUtils.isEmpty(savedArchivePath)) {
                    throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "保存压缩包失败");
                }

                File savedArchiveFile = new File(savedArchivePath);

                // 使用 UUID 文件名作为解压的"容器"
                String unzipFolderName = getFileNameWithoutExtension(savedArchiveFile.getName());
                Path unzipDestPath = folderPath.resolve(unzipFolderName);

                // 2. 执行解压 (带自动去皮逻辑)
                extractArchive(savedArchiveFile, unzipDestPath);

                finalPath = unzipDestPath.toString();

            } else {
                String fileName = FileManageUtil.createFileName(file);
                finalPath = FileManageUtil.uploadFile(file, folderPath, fileName);
            }
        } catch (Exception e) {
            log.error("文件处理异常", e);
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "文件处理失败: " + e.getMessage());
        }

        if (ObjectUtils.isEmpty(finalPath)) {
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "文件处理失败");
        }

        return finalPath.replace(File.separator, "/");
    }

    private void extractArchive(File archiveFile, Path destDirectory) throws Exception {
        String fileName = archiveFile.getName().toLowerCase();

        // 第一步：精准识别公共根目录 (算法优化版)
        String commonRootPrefix = findCommonRootPrefix(archiveFile);
        log.info("压缩包: {}, 识别到的去皮前缀: [{}]", fileName, commonRootPrefix);

        // 第二步：执行解压
        if (fileName.endsWith(".7z")) {
            extract7z(archiveFile, destDirectory, commonRootPrefix);
        } else {
            extractGenericStream(archiveFile, destDirectory, commonRootPrefix);
        }
        flattenSingleTopDirectory(destDirectory);
    }
    /**
     * 如果 destDirectory 下面只有一个子目录且没有其他文件，
     * 则将该子目录中的内容提升到上一层，并删除这个子目录。
     *
     * 例如：
     *  dest/
     *    FengJiWebgl/
     *      Build/
     *      index.html
     *
     * 执行后变为：
     *  dest/
     *    Build/
     *    index.html
     */
    private void flattenSingleTopDirectory(Path destDirectory) throws IOException {
        if (destDirectory == null || !Files.isDirectory(destDirectory)) {
            return;
        }

        List<Path> subDirs = new ArrayList<>();
        boolean hasFile = false;

        // 统计 destDirectory 下的第一层内容
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(destDirectory)) {
            for (Path child : stream) {
                if (Files.isDirectory(child)) {
                    subDirs.add(child);
                } else if (Files.isRegularFile(child)) {
                    hasFile = true;
                }
            }
        }

        // 如果根目录下已经有文件，或者子目录不是唯一的一个，就不做扁平化
        if (hasFile || subDirs.size() != 1) {
            return;
        }

        Path singleDir = subDirs.get(0);

        // 将 singleDir 的所有直接子项移动到 destDirectory 下
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(singleDir)) {
            for (Path child : stream) {
                Path target = destDirectory.resolve(child.getFileName().toString());
                Files.move(child, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // 移除空的壳目录
        Files.delete(singleDir);
    }


    private void extract7z(File archiveFile, Path destDirectory, String commonRootPrefix) throws IOException {
        try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                String entryName = normalizePath(entry.getName());
                // 核心写入逻辑
                writeEntry(destDirectory, entryName, commonRootPrefix, entry.isDirectory(), (out) -> {
                    // 7z 必须循环读取
                    byte[] content = new byte[4096];
                    int count;
                    while ((count = sevenZFile.read(content)) != -1) {
                        out.write(content, 0, count);
                    }
                });
            }
        }
    }

    private void extractGenericStream(File archiveFile, Path destDirectory, String commonRootPrefix) throws Exception {
        try (InputStream is = new BufferedInputStream(new FileInputStream(archiveFile));
             ArchiveInputStream ais = createArchiveInputStream(is, archiveFile.getName())) {

            ArchiveEntry entry;
            while ((entry = ais.getNextEntry()) != null) {
                if (!ais.canReadEntryData(entry)) continue;
                String entryName = normalizePath(entry.getName());

                // 核心写入逻辑
                writeEntry(destDirectory, entryName, commonRootPrefix, entry.isDirectory(), (out) -> {
                    IOUtils.copy(ais, out);
                });
            }
        }
    }

    /**
     * 核心写入方法：负责去皮、跳过根目录本身、安全检查
     */
    private void writeEntry(Path destDirectory, String entryName, String prefix, boolean isDirectory, FileWriterConsumer writer) throws IOException {
        // 1. 过滤系统垃圾文件
        if (isJunkFile(entryName)) return;

        String targetName = entryName;

        // 2. 执行去皮逻辑
        if (StringUtils.isNotEmpty(prefix)) {
            // A. 如果当前条目就是根目录本身 (如 "FengJiWebgl" 或 "FengJiWebgl/")，直接跳过！
            //    这是解决你问题的关键：绝对不能创建这个文件夹
            if (entryName.equals(prefix) || (entryName + "/").equals(prefix) || entryName.equals(prefix.substring(0, prefix.length() - 1))) {
                return;
            }

            // B. 如果是子文件，剥离前缀
            if (entryName.startsWith(prefix)) {
                targetName = entryName.substring(prefix.length());
            } else {
                // 理论上findCommonRootPrefix已拦截，但为了健壮性，不匹配则保持原样
            }
        }

        // 3. 再次检查：如果剥离后名字为空，或者变成了 "/"，跳过
        if (StringUtils.isBlank(targetName) || "/".equals(targetName)) {
            return;
        }

        // 4. 写入文件
        Path filePath = destDirectory.resolve(targetName);
        checkZipSlip(destDirectory, filePath, targetName);

        if (isDirectory) {
            Files.createDirectories(filePath);
        } else {
            if (filePath.getParent() != null && !Files.exists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }
            try (OutputStream out = Files.newOutputStream(filePath)) {
                writer.accept(out);
            }
        }
    }

    /**
     * 终极算法：寻找公共根目录
     * 逻辑：提取所有文件的第一层目录名，放入 Set。如果 Set 大小为 1，且该根确实是文件夹，则返回。
     */
    private String findCommonRootPrefix(File archiveFile) {
        Set<String> rootDirs = new HashSet<>();
        boolean hasDeepPaths = false; // 是否存在 "A/B" 这种深层路径

        String fileName = archiveFile.getName().toLowerCase();

        // 1. 扫描所有文件
        try {
            if (fileName.endsWith(".7z")) {
                try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
                    SevenZArchiveEntry entry;
                    while ((entry = sevenZFile.getNextEntry()) != null) {
                        String name = normalizePath(entry.getName());
                        if (!isJunkFile(name)) {
                            processPathForRootDetection(name, rootDirs);
                            if (name.contains("/")) hasDeepPaths = true;
                        }
                    }
                }
            } else {
                try (InputStream is = new BufferedInputStream(new FileInputStream(archiveFile));
                     ArchiveInputStream ais = createArchiveInputStream(is, archiveFile.getName())) {
                    ArchiveEntry entry;
                    while ((entry = ais.getNextEntry()) != null) {
                        if (ais.canReadEntryData(entry)) {
                            String name = normalizePath(entry.getName());
                            if (!isJunkFile(name)) {
                                processPathForRootDetection(name, rootDirs);
                                if (name.contains("/")) hasDeepPaths = true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("公共前缀检测失败，将采用默认解压", e);
            return null;
        }

        // 2. 判定逻辑
        // 如果存在多个根 (例如 "RootA/..." 和 "RootB/...")，则不去皮
        if (rootDirs.size() != 1) {
            return null;
        }

        // 获取唯一的根
        String singleRoot = rootDirs.iterator().next();

        // 3. 关键校验
        // 如果压缩包里只有平铺的文件 (如 "index.html", "style.css")，hasDeepPaths 为 false，
        // 此时 singleRoot 是 "index.html" (第一个文件名)，我们不应该把它当做目录去皮。
        // 只有当确实检测到了目录结构 (hasDeepPaths=true)，或者这个根本身被作为文件夹明确声明过，才去皮。
        if (hasDeepPaths) {
            return singleRoot + "/";
        }

        return null;
    }

    /**
     * 辅助：提取路径的第一段作为根候选
     * "FengJiWebgl/index.html" -> "FengJiWebgl"
     * "FengJiWebgl" -> "FengJiWebgl"
     */
    private void processPathForRootDetection(String path, Set<String> rootDirs) {
        if (StringUtils.isEmpty(path)) return;
        int slashIndex = path.indexOf('/');
        if (slashIndex == -1) {
            // 这是一个顶级文件或目录
            rootDirs.add(path);
        } else {
            // 这是一个子文件，提取根
            rootDirs.add(path.substring(0, slashIndex));
        }
    }

    /**
     * 路径标准化：统一转为正斜杠，去除 ./ 开头
     */
    private String normalizePath(String path) {
        if (path == null) return null;
        String res = path.replace('\\', '/');
        while (res.startsWith("./")) {
            res = res.substring(2);
        }
        while (res.startsWith("/")) {
            res = res.substring(1);
        }
        return res;
    }

    private boolean isJunkFile(String path) {
        if (path == null) return true;
        return path.contains("__MACOSX") || path.endsWith(".DS_Store") || path.endsWith("Thumbs.db");
    }

    private boolean isSupportedArchive(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name != null && ARCHIVE_EXTENSIONS.stream().anyMatch(ext -> name.toLowerCase().endsWith(ext));
    }

    private String getFileNameWithoutExtension(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".tar.gz")) return fileName.substring(0, fileName.length() - 7);
        if (lower.endsWith(".tgz")) return fileName.substring(0, fileName.length() - 4);
        int dot = fileName.lastIndexOf('.');
        return dot == -1 ? fileName : fileName.substring(0, dot);
    }

    private ArchiveInputStream createArchiveInputStream(InputStream is, String fileName) throws Exception {
        if (!is.markSupported()) is = new BufferedInputStream(is);
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".tar.gz") || lower.endsWith(".tgz")) {
            is = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, is);
            return new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, is);
        }
        return new ArchiveStreamFactory().createArchiveInputStream(is);
    }

    private void checkZipSlip(Path destDir, Path file, String name) throws IOException {
        if (!file.toAbsolutePath().startsWith(destDir.toAbsolutePath())) {
            throw new IOException("非法路径: " + name);
        }
    }

    @FunctionalInterface
    interface FileWriterConsumer {
        void accept(OutputStream out) throws IOException;
    }
}
/*
package com.upc.modular.materials.service.impl;

import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.common.utils.FileManageUtil;
import com.upc.modular.materials.service.IFileUploadService;
import com.upc.modular.materials.entity.TeachingMaterials;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@Service
public class FileUploadServiceImpl implements IFileUploadService {

    // 假设 basePath 是您在类中定义的上传根目录，例如: @Value("${file.upload-path}")
    private final String basePath = "upload";

    @Override
    public String uploadMaterialFile(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "上传文件不能为空");
        }
      */
/*  // 将 "ppt","word", "excel", "pdf" 统一映射为 "file" 类型
        String processedType = type;
        if ("word".equalsIgnoreCase(type) || "excel".equalsIgnoreCase(type) || "pdf".equalsIgnoreCase(type)||"ppt".equalsIgnoreCase(type)) {
            processedType = "file";
        }*//*

        // 校验素材类型是否受支持 (假设您有一个支持的类型列表)
        // 注意：这里不再需要判断 "imageSet" 和 "link"
        if (!TeachingMaterials.SUPPORTED_TYPES.contains(type)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "不支持该素材类型: " + type);
        }
        // 2. 构建文件存储路径
        // 路径结构： basePath/teaching_materials/{type}/{yyyyMMdd}
        // 这里将使用处理后的 processedType ("file") 来创建文件夹
        Path folderPath = Paths.get(basePath, "teaching_materials", type, FileManageUtil.yyyyMMddStr());
        String finalPath;
*/
/*
        // 3. 生成唯一文件名
        String fileName = FileManageUtil.createFileName(file);

        // 4. 执行文件上传
        String filePath = FileManageUtil.uploadFile(file, folderPath, fileName);*//*

        try {
            // 核心逻辑变更：判断 type 是否为 "simulation"
            if ("simulation".equalsIgnoreCase(type) || "H5".equalsIgnoreCase(type)) {
                // --- 'simulation' 类型的处理流程 ---

                // 验证：确保为 simulation 类型上传的是一个ZIP文件
                if (!isZipFile(file)) {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "simulation类型必须上传ZIP压缩包");
                }

                // 步骤 1: 将原始ZIP包作为一个普通文件保存下来
                String originalFileName = FileManageUtil.createFileName(file);
                String savedArchivePath = FileManageUtil.uploadFile(file, folderPath, originalFileName);

                if (ObjectUtils.isEmpty(savedArchivePath)) {
                    throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "保存simulation压缩包失败");
                }

                File savedZipFile = new File(savedArchivePath);


                String savedFileName = savedZipFile.getName();
                String unzipFolderName = savedFileName.substring(0, savedFileName.lastIndexOf('.'));
                Path unzipDestPath = folderPath.resolve(unzipFolderName);

                // 步骤 3: 将ZIP解压到这个新的专属子目录中
                unzip(savedZipFile, unzipDestPath);

                // 结果返回这个新的、更精确的解压子目录的路径
                finalPath = unzipDestPath.toString();

            } else {
                // --- 其他类型（如 word, ppt 等）的处理流程，保持不变 ---
                String fileName = FileManageUtil.createFileName(file);
                finalPath = FileManageUtil.uploadFile(file, folderPath, fileName);
            }
        } catch (IOException e) {
            // e.printStackTrace(); // 生产环境建议使用日志框架
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "文件处理失败: " + e.getMessage());
        }

        if (ObjectUtils.isEmpty(finalPath)) {
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "文件处理失败");
        }

        // 统一返回路径分隔符为'/'
        return finalPath.replace(File.separator, "/");
    }
    */
/**
     * 判断文件是否为ZIP文件
     * 通过MIME类型和文件后缀名双重判断，提高准确性
     *//*

    private boolean isZipFile(MultipartFile file) {
        return "application/zip".equals(file.getContentType()) ||
                (file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().endsWith(".zip"));
    }

    */
/**
     * 将ZIP文件解压到目标路径，并智能去除顶层单一根目录。
     * @param zipFileToUnzip 要解压的ZIP文件
     * @param destDirectory  解压的目标目录
     *//*

    private void unzip(File zipFileToUnzip, Path destDirectory) throws IOException {
        // 使用 try-with-resources 确保 ZipFile 被自动关闭
        try (ZipFile zipFile = new ZipFile(zipFileToUnzip)) {

            // 步骤 1: 分析并找到共同的根目录前缀
            String commonRootPrefix = findCommonRootPrefix(zipFile);

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 步骤 2: 如果找到了共同前缀，就从每个条目名称中移除它
                String targetName = (commonRootPrefix != null)
                        ? entryName.substring(commonRootPrefix.length())
                        : entryName;

                // 如果剥离前缀后名称为空，说明是根目录本身，跳过
                if (targetName.isEmpty()) {
                    continue;
                }

                Path filePath = destDirectory.resolve(targetName);

                // 安全性检查：防止 "Zip Slip" 路径遍历攻击
                if (!filePath.toAbsolutePath().startsWith(destDirectory.toAbsolutePath())) {
                    throw new IOException("解压文件失败：检测到非法路径 " + targetName);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    // 确保父级目录存在
                    if (!filePath.getParent().toFile().exists()){
                        filePath.getParent().toFile().mkdirs();
                    }
                    // 从 ZipFile 中获取输入流并提取文件
                    try (InputStream in = zipFile.getInputStream(entry);
                         FileOutputStream out = new FileOutputStream(filePath.toFile())) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        }
    }

    */
/**
     * 分析ZipFile的所有条目，判断是否存在一个唯一的共同根目录。
     * @return 如果存在，返回共同根目录的名称 (例如 "MyProject/")；否则返回 null。
     *//*

    private String findCommonRootPrefix(ZipFile zipFile) {
        List<String> entryNames = Collections.list(zipFile.entries())
                .stream()
                .map(ZipEntry::getName)
                .collect(Collectors.toList());

        if (entryNames.isEmpty()) {
            return null;
        }

        // 找到第一个条目的第一级目录作为候选根目录
        String firstEntryName = entryNames.get(0);
        int slashIndex = firstEntryName.indexOf('/');
        if (slashIndex <= 0) {
            return null; // 第一个条目就在根目录，不可能有共同根目录
        }

        String potentialPrefix = firstEntryName.substring(0, slashIndex + 1);

        // 检查所有其他条目是否都以这个前缀开头
        for (String name : entryNames) {
            if (!name.startsWith(potentialPrefix)) {
                return null; // 发现不匹配的条目，说明没有唯一的共同根目录
            }
        }

        return potentialPrefix;
    }

    */
/**
     * 从ZipInputStream中提取文件内容并写入
     *//*

    private void extractFile(ZipInputStream zipIn, Path filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = zipIn.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }
}
*/
