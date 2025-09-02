package com.upc.modular.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.Textbook; // 引入Textbook实体
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.TextbookMapper; // 引入TextbookMapper
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/TextbookPackage")
@Api(tags = "教材打包")
public class TextbookPackage {
    @Autowired
    private TextbookCatalogMapper textbookCatalogMapper;
    @Autowired
    private TextbookMapper textbookMapper;

    // 从配置文件注入图片存储的根路径
    @Value("${files.path}")
    private String basePath;

    // private static final String GO_BUILD_WORKSPACE = "D:\\build_workspace";
    private static final String GO_BUILD_WORKSPACE = "/opt/GoBuildWorkspace";
    private static final String RESOURCE_FOLDER_NAME = "resource";
    private static final String IMAGE_SUBFOLDER_NAME = "img";

    /**
     * 从数据库导出 HTML 的时候，会把里面的图片保存到private static String basePath（这里的 basePath 暂定是 D:\workspace\Files，是全局的），
     * 然后会根据教材 id把 basePath 和 id 拼接起来，比如说教材 id=6，那么保存
     * 图片的路径就是 D:\workspace\Files6。
     * 现在的逻辑是：TARGET_PATH 由前端传入，outputBaseName根据 textbookId 从 textbook 表中查出 textbook_name 字段。
     * 在从数据库导出某个 HTML 文件的时候，需要把basePath+教材 id 的那个图片文件夹加到一个 resource文件夹中的 img 文件夹中（因为后期还会有其他资源，比如视频，现在先不用管）。
     * 然后需要把 HTML 中的图片路径改为这个文件夹的相对路径，类似 ./resource/img/1.jpg 这种相对路径），
     * 最后需要把这个 HTML 文件和 resource 文件一起打包
     */


    @ApiOperation(value = "教材打包")
    @PostMapping("/do")
    // 1. 移除 @RequestParam String targetPath
    // 2. 将返回类型修改为 ResponseEntity<Resource>
    public ResponseEntity<Resource> textbookPackage(@RequestParam String targetDeviceID,
                                                    @RequestParam Long textbookId) {
        // ... [查询教材名称的代码保持不变] ...
        Textbook textbook = textbookMapper.selectById(textbookId);
        if (textbook == null) {
            // 对于返回文件的接口，错误处理需要特别设计，这里简化处理
            return ResponseEntity.badRequest().body(null);
        }
        String outputBaseName = textbook.getTextbookName().replaceAll("[\\\\/:*?\"<>|]", "_");

        // 创建一个唯一的临时目录用于存放本次操作的所有文件
        Path temporaryWorkDir = null;
        try {
            temporaryWorkDir = Files.createTempDirectory("package_" + textbookId + "_");

            // 步骤 1 & 2: 在临时目录中生成HTML和资源
            Path tempHtmlPath = generateTextbookHtml(textbookId, temporaryWorkDir, outputBaseName);
            Path sourceImageDir = Paths.get(basePath + textbookId.toString());
            collectLocalResourcesAndRewriteHtml(tempHtmlPath, temporaryWorkDir, sourceImageDir);

            // 步骤 3: 打包和编译，所有输出也都在这个临时目录中
            Path finalPackagePath = packageAndCompile(targetDeviceID, temporaryWorkDir, outputBaseName);

            // 步骤 4: 准备文件下载
            byte[] fileContent = Files.readAllBytes(finalPackagePath);
            Resource resource = new ByteArrayResource(fileContent);
            String downloadFilename = outputBaseName + "_Package.zip";;

            // 设置HTTP头，让浏览器知道这是一个需要下载的文件
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + outputBaseName + "_Package.zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            System.err.println("❌ 处理失败！");
            e.printStackTrace();
            // 实际项目中应返回更友好的错误信息
            return ResponseEntity.internalServerError().body(null);
        } finally {
            // 步骤 5: 清理服务器上的临时目录和文件（非常重要！）
            if (temporaryWorkDir != null) {
                try {
                    deleteDirectoryRecursively(temporaryWorkDir);
                } catch (IOException ex) {
                    System.err.println("警告：未能删除临时文件夹: " + temporaryWorkDir);
                }
            }
        }
    }

    /**
     * NEW: 核心方法 - 收集本地资源并重写HTML路径
     * @param htmlFile         原始HTML文件路径
     * @param outputBaseDir    最终输出的基目录
     * @param sourceImageDir   源图片文件夹的路径 (e.g., D:\workspace\Files6)
     * @return 创建的临时资源文件夹的路径
     */
    private Path collectLocalResourcesAndRewriteHtml(Path htmlFile, Path outputBaseDir, Path sourceImageDir) throws IOException {
        // 在最终输出目录下创建临时的 resource/img 目录结构
        Path tempResourceDir = outputBaseDir.resolve(RESOURCE_FOLDER_NAME);
        Path tempImgDir = tempResourceDir.resolve(IMAGE_SUBFOLDER_NAME);
        Files.createDirectories(tempImgDir);

        // 检查源图片文件夹是否存在
        if (Files.notExists(sourceImageDir) || !Files.isDirectory(sourceImageDir)) {
            System.out.println("  - 警告: 源图片文件夹不存在或不是一个目录，将创建空的资源包: " + sourceImageDir);
        } else {
            // 将整个源图片文件夹内容复制到临时的 img 文件夹中
//            System.out.println("  - 正在从 " + sourceImageDir + " 复制图片到 " + tempImgDir);
            copyDirectory(sourceImageDir, tempImgDir);
        }

        // 读取HTML并重写路径
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlFile.toFile(), "UTF-8", "");
        Elements images = doc.select("img");

        for (Element img : images) {
            String originalSrc = img.attr("src");
            if (originalSrc != null && !originalSrc.isEmpty()) {
                // 从绝对路径中只提取文件名
                String fileName = Paths.get(originalSrc).getFileName().toString();
                // 构建新的相对路径
                String newRelativeSrc = RESOURCE_FOLDER_NAME + "/" + IMAGE_SUBFOLDER_NAME + "/" + fileName;
                img.attr("src", newRelativeSrc);
//                System.out.println("  - 路径更新: '" + originalSrc + "' -> '" + newRelativeSrc + "'");
            }
        }

        // 将修改后的HTML内容写回原文件
        Files.write(htmlFile, doc.outerHtml().getBytes(StandardCharsets.UTF_8));
        return tempResourceDir;
    }

    /**
     * 辅助方法 - 递归复制文件夹
     */
    private void copyDirectory(Path source, Path dest) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(sourcePath -> {
                try {
                    Path targetPath = dest.resolve(source.relativize(sourcePath));
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("无法复制文件: " + sourcePath, e);
                }
            });
        }
    }

    /**
     * 辅助方法 - 递归删除文件夹
     */
    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        }
    }

    /**
     * 生成教材HTML文件，并将其保存在指定的工作目录中。
     * @param textbookId 教材ID
     * @param workDir 本次请求的唯一临时工作目录
     * @param outputBaseName 教材的名称，用作HTML文件名
     * @return 生成的HTML文件的路径
     * @throws IOException 如果文件操作失败或未找到内容
     */
    private Path generateTextbookHtml(Long textbookId, Path workDir, String outputBaseName) throws IOException {
        LambdaQueryWrapper<TextbookCatalog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TextbookCatalog::getTextbookId, textbookId);
        lambdaQueryWrapper.orderByAsc(TextbookCatalog::getSort);
        List<TextbookCatalog> textbookCatalogs = textbookCatalogMapper.selectList(lambdaQueryWrapper);

        if (textbookCatalogs.isEmpty()) {
            throw new IOException("未找到ID为 " + textbookId + " 的教材内容。");
        }

        // --- [后续的HTML内容拼接逻辑保持不变] ---
        List<String> htmlFragments = textbookCatalogs.stream()
                .flatMap(catalog -> Stream.of("<h2>" + catalog.getCatalogName() + "</h2>", catalog.getContent()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset='UTF-8'><title>").append(outputBaseName).append("</title></head><body>");
        for (String fragment : htmlFragments) {
            htmlBuilder.append(fragment).append("\n");
        }
        htmlBuilder.append("</body></html>");

        String mergedHtml = sanitizeHtml(htmlBuilder.toString());

        // --- [核心修改点：不再创建随机临时文件，而是在指定工作目录中创建固定名称的文件] ---
        Path htmlFile = workDir.resolve(outputBaseName + ".html");
        Files.write(htmlFile, mergedHtml.getBytes(StandardCharsets.UTF_8));

        return htmlFile;
    }

    private String sanitizeHtml(String html) {
        // 使用 Jsoup 清洗可能嵌套的 body/head 等结构
        org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(html);
        doc.outputSettings(new org.jsoup.nodes.Document.OutputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.html).escapeMode(Entities.EscapeMode.xhtml));
        return doc.html();
    }

    /**
     * 核心打包编译方法，现在返回最终打包好的文件路径
     * @return 包含.exe和加密.zip的总包路径
     */
    public Path packageAndCompile(String deviceId, Path workDir, String outputBaseName) throws Exception {
        String password = generateRandomPassword(12);
        System.out.println(" -> 1: 生成随机密码 -> " + password);

        // 在工作目录中找到HTML文件和resource文件夹
        Path mainFile = workDir.resolve(outputBaseName + ".html"); // 假设 generateTextbookHtml 已按此命名
        Path resourceDir = workDir.resolve(RESOURCE_FOLDER_NAME);

        // 1. 创建加密的教材内容ZIP包
        Path contentZipPath = workDir.resolve(outputBaseName + "_Content.zip");
        createEncryptedZip(mainFile, resourceDir, contentZipPath, password);
        System.out.println(" -> 2: 创建加密内容ZIP包 -> " + contentZipPath.toAbsolutePath());

        // 2. 编译Go解锁程序
        Path exeOutputPath = workDir.resolve(outputBaseName + "_Unlocker.exe");
        compileGoExecutable(deviceId, password, exeOutputPath);
        System.out.println(" -> 3: 编译Go可执行文件 -> " + exeOutputPath.toAbsolutePath());

        // 3. 将上面两步生成的文件，打包成一个最终的ZIP包供用户下载
        Path finalPackagePath = workDir.resolve(outputBaseName + "_Package.zip");
        System.out.println(" -> 4: 创建最终下载包 -> " + finalPackagePath.toAbsolutePath());
        try (ZipFile finalZip = new ZipFile(finalPackagePath.toFile())) {
            finalZip.addFile(contentZipPath.toFile());
            finalZip.addFile(exeOutputPath.toFile());
        }

        return finalPackagePath;
    }

    /**
     * 生成随机密码
     */
    private static String generateRandomPassword(int length) {
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * 创建加密的ZIP文件
     */
    private static void createEncryptedZip(Path mainFile, Path resourceDir, Path zipOutputPath, String password) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipOutputPath.toFile(), password.toCharArray())) {
            ZipParameters params = new ZipParameters();
            params.setEncryptFiles(true);
            params.setEncryptionMethod(EncryptionMethod.AES);

            zipFile.addFile(mainFile.toFile(), params);
            System.out.println("    - 添加文件到ZIP: " + mainFile.getFileName());

            zipFile.addFolder(resourceDir.toFile(), params);
            System.out.println("    - 添加文件夹到ZIP: " + resourceDir.getFileName());
        }
    }

    /**
     * 编译Go程序
     */
    private static void compileGoExecutable(String deviceId, String password, Path exeOutputPath) throws IOException, InterruptedException {
        // 确保 GO_BUILD_WORKSPACE 是正确的服务器路径, 例如 "/opt/GoBuildWorkspace"
        final String GO_BUILD_WORKSPACE = "/opt/GoBuildWorkspace";
        Path workspaceDir = Paths.get(GO_BUILD_WORKSPACE);
        if (!Files.isDirectory(workspaceDir)) {
            throw new IOException("Go编译工作区不存在，请先手动创建: " + workspaceDir);
        }

        String templateContent;
        InputStream inputStream = TextbookPackage.class.getResourceAsStream("/template.go");
        if (inputStream == null) {
            throw new IOException("无法在类路径中找到资源文件: template.go");
        }

        // 使用Java 8兼容的方式读取流
        try (InputStream is = inputStream) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            byte[] allBytes = buffer.toByteArray();
            templateContent = new String(allBytes, StandardCharsets.UTF_8);
        }

        // 替换占位符
        String finalGoCode = templateContent
                .replace("DEVICE_CODE_PLACEHOLDER", deviceId)
                .replace("PASSWORD_PLACEHOLDER", password);

        // 创建临时Go源文件
        Path tempGoFile = Files.createTempFile(workspaceDir, "unlocker_", ".go");
        Files.write(tempGoFile, finalGoCode.getBytes(StandardCharsets.UTF_8));

        // 准备并执行编译命令
        // 注意：这里的编译参数是为Windows准备的，部署到Linux时需要修改
        ProcessBuilder pb = new ProcessBuilder(
                "go", "build", "-ldflags=-H=windowsgui", "-o", exeOutputPath.toString(), tempGoFile.getFileName().toString()
        );
        pb.directory(workspaceDir.toFile());
        pb.inheritIO();
        System.out.println(" ... 开始编译Go程序，工作目录: " + workspaceDir + " ...");
        Process process = pb.start();
        int exitCode = process.waitFor();
        Files.delete(tempGoFile);
        if (exitCode != 0) {
            throw new RuntimeException("Go编译失败！请检查Go和C++编译器环境是否配置正确，以及Go命令是否在系统PATH中。");
        }
    }

    /**
     * 0.https://g.co/gemini/share/727f3a4a25ff
     * 1.安装go语言的运行环境-https://go.dev/dl/（例如win版本go1.22.5.windows-amd64.msi）
     *   1.1.PATH环境变量
     *   1.2.安装C编译器和构建工具 (GCC/build-essential)：
     *      zenity库需要调用系统的C语言库来创建图形界面，因此必须安装C编译器。
     *   1.3.安装GUI库的开发包 ：
     *      zenity依赖GTK等图形库。在服务器上，即使不显示GUI，编译时也需要这些库的头文件。
     * 2.查询硬件码的方法：wmic csproduct get uuid；Linux：cat /sys/class/dmi/id/product_uuid。
     * 3.*创建一个go的“编译工作区”
     *   3.1. 创建并进入该目录
     *      mkdir C:\GoBuildWorkspace
     *      cd C:\GoBuildWorkspace
     *   3.2.初始化Go Modules，给项目起个名字，例如 build_workspace
     *      go mod init build_workspace
     *   3.3. 手动下载并注册zenity库到这个工作区
     *      （go env -w GOPROXY=https://goproxy.cn,direct）
     *      go get github.com/ncruces/zenity
     */
}