package com.upc.modular.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@RestController
@RequestMapping("/TextbookPackage")
@Api(tags = "教材打包 (含资源版)")
public class TextbookPackage {

    @Autowired
    private TextbookCatalogMapper textbookCatalogMapper;
    @Autowired
    private TextbookMapper textbookMapper;

    // Go 语言编译的工作区
    private static final String GO_BUILD_WORKSPACE = "/opt/GoBuildWorkspace";

    // *** UPDATED ***: Base path for textbook pictures updated to the specific server path.
    private static final String TEXTBOOK_PICTURE_BASE_PATH = "/opt/textbook-app/upload/public/picture/convertTextbookImage/";

    @ApiOperation(value = "教材打包（包含图片等资源）")
    @PostMapping("/do")
    public ResponseEntity<Resource> textbookPackage(@RequestParam String targetDeviceID,
                                                    @RequestParam Long textbookId) {
        // 1. 根据教材ID查询教材，用于命名文件及查找资源
        Textbook textbook = textbookMapper.selectById(textbookId);
        if (textbook == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材不存在");
        }
        String outputBaseName = textbook.getTextbookName().replaceAll("[\\\\/:*?\"<>|]", "_");

        // 2. 创建唯一的临时工作目录
        Path temporaryWorkDir = null;
        try {
            temporaryWorkDir = Files.createTempDirectory("package_textbook_" + textbookId + "_");
            System.out.println("创建临时工作目录: " + temporaryWorkDir);

            // 3. 从数据库获取内容, 复制资源并生成一个临时的HTML文件
            Path textbookHtmlPath = generateTextbookHtmlWithResources(textbookId, temporaryWorkDir, outputBaseName);

            // 4. 打包HTML及资源文件、编译Go解锁程序，并生成最终的包
            Path finalPackagePath = packageAndCompile(targetDeviceID, textbookHtmlPath, temporaryWorkDir, outputBaseName);

            // 5. 读取最终的包文件，准备HTTP响应
            Resource resource = new ByteArrayResource(Files.readAllBytes(finalPackagePath));
            String downloadFilename = outputBaseName + "_Package.zip";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFilename + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            System.err.println("教材打包失败！Textbook ID: " + textbookId);
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        } finally {
            // 6. 清理服务器上的临时目录和文件
            if (temporaryWorkDir != null) {
                try {
                    deleteDirectoryRecursively(temporaryWorkDir);
                    System.out.println("已清理临时工作目录: " + temporaryWorkDir);
                } catch (IOException ex) {
                    System.err.println("警告：未能成功删除临时文件夹: " + temporaryWorkDir);
                }
            }
        }
    }

    /**
     * 从数据库查询教材内容，复制相关资源（图片），修改HTML中的路径，并最终生成HTML文件。
     *
     * @param textbookId     教材ID
     * @param workDir        本次请求的唯一临时工作目录
     * @param outputBaseName 教材名称，用作HTML文件名
     * @return 生成的HTML文件的路径
     * @throws IOException 如果文件操作失败或未找到内容
     */
    private Path generateTextbookHtmlWithResources(Long textbookId, Path workDir, String outputBaseName) throws IOException {
        // 查询数据库获取章节内容
        LambdaQueryWrapper<TextbookCatalog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookCatalog::getTextbookId, textbookId);
        queryWrapper.orderByAsc(TextbookCatalog::getSort);
        List<TextbookCatalog> textbookCatalogs = textbookCatalogMapper.selectList(queryWrapper);

        if (textbookCatalogs.isEmpty()) {
            throw new IOException("数据库中未找到ID为 " + textbookId + " 的教材内容。");
        }

        // 拼接成一个完整的HTML文档
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset='UTF-8'><title>").append(outputBaseName).append("</title></head><body>");

        textbookCatalogs.forEach(catalog -> {
            if (catalog != null && catalog.getCatalogName() != null) {
                htmlBuilder.append(catalog.getCatalogName());
            }
            if (catalog.getContent() != null) {
                htmlBuilder.append(catalog.getContent());
            }
            htmlBuilder.append("\n");
        });
        htmlBuilder.append("</body></html>");

        // --- NEW LOGIC: Copy resources and update HTML paths ---
        // 1. 定义源图片目录和目标资源目录
        Path sourceImageDir = Paths.get(TEXTBOOK_PICTURE_BASE_PATH, String.valueOf(textbookId));
        Path targetResourceDir = workDir.resolve("resource");
        Path targetImgDir = targetResourceDir.resolve("img");

        String finalHtmlContent = htmlBuilder.toString();

        // 2. 如果源图片目录存在，则复制并修改HTML
        if (Files.exists(sourceImageDir) && Files.isDirectory(sourceImageDir)) {
            System.out.println("  - 发现资源目录: " + sourceImageDir);
            Files.createDirectories(targetImgDir); // 创建 resource/img 文件夹

            // 复制整个图片目录到临时工作区的 resource/img 下
            try (Stream<Path> stream = Files.walk(sourceImageDir)) {
                stream.forEach(source -> {
                    try {
                        Path destination = targetImgDir.resolve(sourceImageDir.relativize(source));
                        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println("警告：复制文件失败 " + source + " -> " + e.getMessage());
                    }
                });
            }
            System.out.println("  - 已将资源文件复制到: " + targetImgDir);

            // 3. 使用Jsoup解析HTML并修改图片路径为相对路径
            Document doc = Jsoup.parse(finalHtmlContent);
            Elements images = doc.select("img");
            for (Element img : images) {
                String src = img.attr("src");
                // 从绝对路径中提取文件名
                String fileName = new File(src).getName();
                // 更新为新的相对路径
                img.attr("src", "./resource/img/" + fileName);
            }
            finalHtmlContent = doc.html();
            System.out.println("  - 已更新HTML中的图片路径为相对路径");
        } else {
            System.out.println("  - 未找到资源目录: " + sourceImageDir + ", 将直接打包纯文本HTML。");
        }

        // 4. 在指定工作目录中创建最终的HTML文件
        Path htmlFilePath = workDir.resolve(outputBaseName + ".html");
        Files.write(htmlFilePath, finalHtmlContent.getBytes(StandardCharsets.UTF_8));
        System.out.println("  - 已生成HTML文件: " + htmlFilePath);
        return htmlFilePath;
    }

    /**
     * 核心流程：打包内容（HTML和资源）、编译程序、生成最终包。
     *
     * @param deviceId         目标设备ID
     * @param textbookHtmlPath 要打包的HTML文件路径
     * @param workDir          工作目录
     * @param outputBaseName   文件基础名称
     * @return 最终生成的总包路径
     */
    public Path packageAndCompile(String deviceId, Path textbookHtmlPath, Path workDir, String outputBaseName) throws Exception {
        // 1. 生成随机密码
        String password = generateRandomPassword(12);
        System.out.println(" -> 1. 生成随机密码: " + password);

        // 2. 将HTML文件和resource文件夹创建为一个加密的ZIP包
        Path contentZipPath = workDir.resolve(outputBaseName + "_Content.zip");
        createEncryptedZipWithResources(textbookHtmlPath, workDir.resolve("resource"), contentZipPath, password);
        System.out.println(" -> 2. 已创建加密的内容ZIP包: " + contentZipPath);

        // 3. 编译Go语言的解锁程序
        Path exeOutputPath = workDir.resolve(outputBaseName + "_Unlocker.exe");
        compileGoExecutable(deviceId, password, exeOutputPath);
        System.out.println(" -> 3. 已编译Go可执行文件: " + exeOutputPath);

        // 4. 将加密内容ZIP包和Go解锁程序打包成一个最终的ZIP包
        Path finalPackagePath = workDir.resolve(outputBaseName + "_Package.zip");
        System.out.println(" -> 4. 创建最终下载包: " + finalPackagePath);
        try (ZipFile finalZip = new ZipFile(finalPackagePath.toFile())) {
            finalZip.addFile(contentZipPath.toFile());
            finalZip.addFile(exeOutputPath.toFile());
        }

        return finalPackagePath;
    }

    /**
     * 创建一个包含HTML文件和整个resource文件夹的加密ZIP。
     */
    private void createEncryptedZipWithResources(Path htmlFile, Path resourceDir, Path zipOutputPath, String password) throws IOException {
        ZipParameters params = new ZipParameters();
        params.setEncryptFiles(true);
        params.setEncryptionMethod(EncryptionMethod.AES);
        params.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

        try (ZipFile zipFile = new ZipFile(zipOutputPath.toFile(), password.toCharArray())) {
            // 添加HTML文件
            zipFile.addFile(htmlFile.toFile(), params);

            // 如果resource文件夹存在，则添加整个文件夹
            if (Files.exists(resourceDir) && Files.isDirectory(resourceDir)) {
                zipFile.addFolder(resourceDir.toFile(), params);
            }
        }
    }

    // =================================================================
    // 以下方法与原始代码相同，无需修改
    // =================================================================

    private String generateRandomPassword(int length) {
        final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private void compileGoExecutable(String deviceId, String password, Path exeOutputPath) throws IOException, InterruptedException {
        Path workspaceDir = Paths.get(GO_BUILD_WORKSPACE);
        if (!Files.isDirectory(workspaceDir)) {
            throw new IOException("Go编译工作区不存在，请在服务器上手动创建: " + workspaceDir);
        }

        String templateContent;
        try (InputStream inputStream = TextbookPackage.class.getResourceAsStream("/template.go")) {
            if (inputStream == null) {
                throw new IOException("无法在类路径中找到Go模板文件: /template.go");
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            templateContent = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }

        String finalGoCode = templateContent
                .replace("DEVICE_CODE_PLACEHOLDER", deviceId)
                .replace("PASSWORD_PLACEHOLDER", password);

        Path tempGoFile = Files.createTempFile(workspaceDir, "unlocker_", ".go");
        Files.write(tempGoFile, finalGoCode.getBytes(StandardCharsets.UTF_8));

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "go", "build",
                    "-ldflags=-H=windowsgui",
                    "-o", exeOutputPath.toString(),
                    tempGoFile.getFileName().toString()
            );

            pb.environment().put("GOOS", "windows");
            pb.environment().put("GOARCH", "amd64");
            pb.environment().put("CGO_ENABLED", "0");
            pb.directory(workspaceDir.toFile());

            System.out.println(" ... 开始编译Go程序，工作目录: " + workspaceDir + " ...");
            Process process = pb.start();

            try (InputStream processInputStream = process.getInputStream()) {
                ByteArrayOutputStream processOutputBuffer = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int nRead;
                while ((nRead = processInputStream.read(data, 0, data.length)) != -1) {
                    processOutputBuffer.write(data, 0, nRead);
                }
                processOutputBuffer.flush();
                String result = new String(processOutputBuffer.toByteArray(), StandardCharsets.UTF_8);
                if (result != null && !result.trim().isEmpty()) {
                    System.out.println("Go build output:\n" + result);
                }
            }

            if (!process.waitFor(5, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                throw new InterruptedException("Go 编译超时（超过5分钟）");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new RuntimeException("Go 编译失败！退出码: " + exitCode);
            }
        } finally {
            Files.deleteIfExists(tempGoFile);
        }
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }

    // This method was unused in the original code, keeping it for reference
    private String sanitizeHtml(String html) {
        org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(html);
        doc.outputSettings(new org.jsoup.nodes.Document.OutputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.html));
        return doc.body().html();
    }
}