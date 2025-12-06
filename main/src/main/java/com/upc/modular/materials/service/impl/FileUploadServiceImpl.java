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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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

                // 修改：返回原始压缩包路径而不是解压目录路径
                //finalPath = savedArchivePath;
                //返回解压后的目录路径
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

        // 收集真实子目录和文件（忽略垃圾文件/目录）
        List<Path> realSubDirs = new ArrayList<>();
        List<Path> realFiles = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(destDirectory)) {
            for (Path child : stream) {
                // 过滤垃圾目录/文件
                String fileName = child.getFileName().toString();
                if (fileName.startsWith(".") || "Thumbs.db".equals(fileName) || "__MACOSX".equals(fileName)) {
                    continue;
                }

                if (Files.isDirectory(child)) {
                    realSubDirs.add(child);
                } else if (Files.isRegularFile(child)) {
                    realFiles.add(child);
                }
            }
        }

        // 如果根目录下已经有真实文件，或者真实子目录不是唯一的一个，就不做扁平化
        if (!realFiles.isEmpty() || realSubDirs.size() != 1) {
            return;
        }

        Path singleDir = realSubDirs.get(0);

        // 记录日志
        log.debug("Flattening directory: {} -> {}", singleDir, destDirectory);

        try {
            // 遍历 singleDir 下的所有内容并移动到 destDirectory
            Files.walk(singleDir)
                .filter(path -> !path.equals(singleDir)) // 排除 singleDir 本身
                .sorted(
                    Comparator
                        .comparingInt((Path p) -> p.getNameCount())
                        .reversed()
                        .thenComparing(Path::toString) // 先处理深层路径再处理浅层路径
                )
                .forEach(path -> {
                    try {
                        Path relativePath = singleDir.relativize(path);
                        Path targetPath = destDirectory.resolve(relativePath);

                        // 确保目标父目录存在
                        if (Files.isDirectory(path)) {
                            Files.createDirectories(targetPath);
                        } else {
                            if (targetPath.getParent() != null && !Files.exists(targetPath.getParent())) {
                                Files.createDirectories(targetPath.getParent());
                            }
                            // 移动文件，必要时覆盖
                            Files.move(path, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

            // 使用 walkFileTree 自底向上删除空的 singleDir
            Files.walkFileTree(singleDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

            log.debug("Successfully flattened directory: {}", destDirectory);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new IOException("Error during directory flattening", e);
        }
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
     * 支持UTF-8和GBK编码的双重尝试机制，以处理不同系统打包的zip文件。
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
                // 首先尝试使用UTF-8编码处理ZIP文件
                try {
                    processZipFileWithCharset(archiveFile, rootDirs, hasDeepPaths, StandardCharsets.UTF_8);
                } catch (IllegalArgumentException e) {
                    // 如果UTF-8解码失败，尝试使用GBK编码
                    try {
                        processZipFileWithCharset(archiveFile, rootDirs, hasDeepPaths, Charset.forName("GBK"));
                    } catch (Exception gbkException) {
                        log.warn("使用GBK编码处理ZIP文件时发生异常: {}", gbkException.getMessage());
                        // 回退到通用处理方法
                        processGenericArchive(archiveFile, rootDirs, hasDeepPaths);
                    }
                } catch (Exception utf8Exception) {
                    log.warn("使用UTF-8编码处理ZIP文件时发生异常: {}", utf8Exception.getMessage());
                    // 回退到通用处理方法
                    processGenericArchive(archiveFile, rootDirs, hasDeepPaths);
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
     * 使用指定字符集处理ZIP文件
     */
    private void processZipFileWithCharset(File archiveFile, Set<String> rootDirs, boolean hasDeepPaths, Charset charset) throws IOException {
        try (ZipFile zipFile = new ZipFile(archiveFile, charset)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                try {
                    ZipEntry entry = entries.nextElement();
                    if (entry != null) {
                        String name = normalizePath(entry.getName());
                        if (!isJunkFile(name)) {
                            processPathForRootDetection(name, rootDirs);
                            if (name.contains("/")) hasDeepPaths = true;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("在处理ZIP条目时遇到编码问题，跳过该条目");
                    continue;
                }
            }
        }
    }

    /**
     * 处理通用归档文件格式
     */
    private void processGenericArchive(File archiveFile, Set<String> rootDirs, boolean hasDeepPaths) throws Exception {
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
