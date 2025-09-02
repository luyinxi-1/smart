package com.upc.modular.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.jsoup.nodes.Entities;
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
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/TextbookPackage")
@Api(tags = "教材打包 (纯文本版)")
public class TextbookPackage {

    @Autowired
    private TextbookCatalogMapper textbookCatalogMapper;
    @Autowired
    private TextbookMapper textbookMapper;

    // Go 语言编译的工作区，必须在服务器上存在且可写
    private static final String GO_BUILD_WORKSPACE = "/opt/GoBuildWorkspace";

    @ApiOperation(value = "教材打包（纯文本内容）")
    @PostMapping("/do")
    public ResponseEntity<Resource> textbookPackage(@RequestParam String targetDeviceID,
                                                    @RequestParam Long textbookId) {
        // 1. 根据教材ID查询教材名称，用于命名文件
        Textbook textbook = textbookMapper.selectById(textbookId);
        if (textbook == null) {
            // 在生产环境中，最好返回一个包含错误信息的JSON体
            return ResponseEntity.badRequest().body(null);
        }
        // 清理文件名中的非法字符
        String outputBaseName = textbook.getTextbookName().replaceAll("[\\\\/:*?\"<>|]", "_");

        // 2. 创建一个唯一的临时工作目录，用于存放本次操作生成的所有文件
        Path temporaryWorkDir = null;
        try {
            temporaryWorkDir = Files.createTempDirectory("package_textbook_" + textbookId + "_");
            System.out.println("创建临时工作目录: " + temporaryWorkDir);

            // 3. 从数据库获取内容并生成一个临时的HTML文件
            Path textbookHtmlPath = generateTextbookHtml(textbookId, temporaryWorkDir, outputBaseName);

            // 4. 打包HTML文件、编译Go解锁程序，并生成最终的包
            Path finalPackagePath = packageAndCompile(targetDeviceID, textbookHtmlPath, temporaryWorkDir, outputBaseName);

            // 5. 读取最终的包文件，准备HTTP响应
            Resource resource = new ByteArrayResource(Files.readAllBytes(finalPackagePath));
            String downloadFilename = outputBaseName + "_Package.zip";

            // 设置HTTP头，使浏览器触发下载
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFilename + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            System.err.println("❌ 教材打包失败！Textbook ID: " + textbookId);
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        } finally {
            // 6. 清理服务器上的临时目录和文件（关键步骤！）
            if (temporaryWorkDir != null) {
                try {
                    deleteDirectoryRecursively(temporaryWorkDir);
                    System.out.println("已清理临时工作目录: " + temporaryWorkDir);
                } catch (IOException ex) {
                    System.err.println("⚠️ 警告：未能成功删除临时文件夹: " + temporaryWorkDir);
                }
            }
        }
    }

    /**
     * 从数据库查询教材内容，并生成一个HTML文件。
     *
     * @param textbookId     教材ID
     * @param workDir        本次请求的唯一临时工作目录
     * @param outputBaseName 教材名称，用作HTML文件名
     * @return 生成的HTML文件的路径
     * @throws IOException 如果文件操作失败或未找到内容
     */
    private Path generateTextbookHtml(Long textbookId, Path workDir, String outputBaseName) throws IOException {
        LambdaQueryWrapper<TextbookCatalog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookCatalog::getTextbookId, textbookId);
        queryWrapper.orderByAsc(TextbookCatalog::getSort);
        List<TextbookCatalog> textbookCatalogs = textbookCatalogMapper.selectList(queryWrapper);

        if (textbookCatalogs.isEmpty()) {
            throw new IOException("数据库中未找到ID为 " + textbookId + " 的教材内容。");
        }

        // 将所有章节内容拼接成一个完整的HTML文档
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset='UTF-8'><title>").append(outputBaseName).append("</title></head><body>");

        textbookCatalogs.stream()
                .filter(c -> c != null && c.getCatalogName() != null)
                .forEach(catalog -> {
                    htmlBuilder.append("<h2>").append(catalog.getCatalogName()).append("</h2>");
                    if (catalog.getContent() != null) {
                        htmlBuilder.append(catalog.getContent());
                    }
                    htmlBuilder.append("\n");
                });

        htmlBuilder.append("</body></html>");

        String mergedHtml = sanitizeHtml(htmlBuilder.toString());

        // 在指定工作目录中创建HTML文件
        Path htmlFilePath = workDir.resolve(outputBaseName + ".html");
        Files.write(htmlFilePath, mergedHtml.getBytes(StandardCharsets.UTF_8));
        System.out.println("  - 已生成HTML文件: " + htmlFilePath);
        return htmlFilePath;
    }

    /**
     * 清理HTML，防止多余的<html>, <body>标签嵌套。
     */
    private String sanitizeHtml(String html) {
        org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(html);
        doc.outputSettings(new org.jsoup.nodes.Document.OutputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.html).escapeMode(Entities.EscapeMode.xhtml));
        return doc.body().html();
    }

    /**
     * 核心流程：打包内容、编译程序、生成最终包。
     *
     * @param deviceId         目标设备ID
     * @param textbookHtmlPath 要打包的HTML文件路径
     * @param workDir          工作目录
     * @param outputBaseName   文件基础名称
     * @return 最终生成的总包路径
     */
    public Path packageAndCompile(String deviceId, Path textbookHtmlPath, Path workDir, String outputBaseName) throws Exception {
        // 1. 生成一个12位的随机密码
        String password = generateRandomPassword(12);
        System.out.println(" -> 1. 生成随机密码: " + password);

        // 2. 将HTML文件创建为一个加密的ZIP包
        Path contentZipPath = workDir.resolve(outputBaseName + "_Content.zip");
        createEncryptedZip(textbookHtmlPath, contentZipPath, password);
        System.out.println(" -> 2. 已创建加密的内容ZIP包: " + contentZipPath);

        // 3. 编译Go语言的解锁程序
        Path exeOutputPath = workDir.resolve(outputBaseName + "_Unlocker.exe");
        compileGoExecutable(deviceId, password, exeOutputPath);
        System.out.println(" -> 3. 已编译Go可执行文件: " + exeOutputPath);

        // 4. 将加密内容ZIP包和Go解锁程序打包成一个最终的ZIP包，供用户下载
        Path finalPackagePath = workDir.resolve(outputBaseName + "_Package.zip");
        System.out.println(" -> 4. 创建最终下载包: " + finalPackagePath);
        try (ZipFile finalZip = new ZipFile(finalPackagePath.toFile())) {
            finalZip.addFile(contentZipPath.toFile());
            finalZip.addFile(exeOutputPath.toFile());
        }

        return finalPackagePath;
    }

    /**
     * 生成指定长度的随机密码。
     */
    private String generateRandomPassword(int length) {
        final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * 创建一个只包含单个文件的加密ZIP。
     */
    private void createEncryptedZip(Path fileToZip, Path zipOutputPath, String password) throws IOException {
        ZipParameters params = new ZipParameters();
        params.setEncryptFiles(true);
        params.setEncryptionMethod(EncryptionMethod.AES);
        params.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

        try (ZipFile zipFile = new ZipFile(zipOutputPath.toFile(), password.toCharArray())) {
            zipFile.addFile(fileToZip.toFile(), params);
        }
    }

    /**
     * 动态修改Go模板并编译成Windows可执行文件。
     */
    private void compileGoExecutable(String deviceId, String password, Path exeOutputPath) throws IOException, InterruptedException {
        Path workspaceDir = Paths.get(GO_BUILD_WORKSPACE);
        if (!Files.isDirectory(workspaceDir)) {
            throw new IOException("Go编译工作区不存在，请在服务器上手动创建: " + workspaceDir);
        }

        // 从类路径（resources目录）读取Go模板文件
        String templateContent;
        try (InputStream inputStream = TextbookPackage.class.getResourceAsStream("/template.go")) {
            if (inputStream == null) {
                throw new IOException("无法在类路径中找到Go模板文件: /template.go");
            }

            // --- [修改点 1: 替换 InputStream.readAllBytes()] ---
            // Java 8 的标准写法：使用 ByteArrayOutputStream 来读取所有字节
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024]; // 可以选择合适的缓冲区大小
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            templateContent = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
            // --- [修改结束] ---
        }

        // 替换模板中的占位符
        String finalGoCode = templateContent
                .replace("DEVICE_CODE_PLACEHOLDER", deviceId)
                .replace("PASSWORD_PLACEHOLDER", password);

        // 将修改后的代码写入Go编译工作区的一个临时.go文件
        Path tempGoFile = Files.createTempFile(workspaceDir, "unlocker_", ".go");
        Files.write(tempGoFile, finalGoCode.getBytes(StandardCharsets.UTF_8));

        try {
            // 准备并执行交叉编译命令
            ProcessBuilder pb = new ProcessBuilder(
                    "go", "build",
                    "-ldflags=-H=windowsgui", // 此标志用于在Windows上运行时隐藏命令行窗口
                    "-o", exeOutputPath.toString(),
                    tempGoFile.getFileName().toString()
            );

            // **关键**：设置环境变量以在Linux上编译Windows程序
            pb.environment().put("GOOS", "windows");
            pb.environment().put("GOARCH", "amd64");
            pb.environment().put("CGO_ENABLED", "0"); // 禁用CGO，对交叉编译很重要

            pb.directory(workspaceDir.toFile()); // 设置命令执行的目录

            System.out.println(" ... 开始编译Go程序，工作目录: " + workspaceDir + " ...");
            Process process = pb.start();

            // 捕获编译过程的输出，用于调试
            String result;
            try (InputStream processInputStream = process.getInputStream()) {
                // --- [修改点 1 的另一个应用] ---
                ByteArrayOutputStream processOutputBuffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = processInputStream.read(data, 0, data.length)) != -1) {
                    processOutputBuffer.write(data, 0, nRead);
                }
                processOutputBuffer.flush();
                result = new String(processOutputBuffer.toByteArray(), StandardCharsets.UTF_8);
                // --- [修改结束] ---
            }

            // --- [修改点 2: 替换 String.isBlank()] ---
            // Java 8 的等效写法：检查是否为 null 或 trim后是否为空
            if (result != null && !result.trim().isEmpty()) {
                System.out.println("Go build output:\n" + result);
            }
            // --- [修改结束] ---


            // 等待编译完成，设置5分钟超时
            if (!process.waitFor(5, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                throw new InterruptedException("Go 编译超时（超过5分钟）");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new RuntimeException("Go 编译失败！退出码: " + exitCode + "。请检查服务器Go环境及C编译器是否配置正确。");
            }
        } finally {
            // 确保临时Go源文件被删除
            Files.deleteIfExists(tempGoFile);
        }
    }
    /**
     * 辅助方法：递归删除文件夹及其所有内容。
     */
    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }
}