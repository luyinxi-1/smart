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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
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

    private static final String GO_BUILD_WORKSPACE = "D:\\build_workspace";
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
    public R textbookPackage(@RequestParam String targetDeviceID,
                             @RequestParam Long textbookId,
                             @RequestParam String targetPath) {

        if (StringUtils.isEmpty(targetPath)) {
            return R.fail("错误：必须提供一个有效的目标输出路径 (targetPath)！");
        }
        if (targetDeviceID == null || targetDeviceID.isEmpty() || "YOUR_WINDOWS_UUID_HERE".equals(targetDeviceID)) {
            return R.fail("错误：请提供一个有效的目标设备ID (targetDeviceID)！");
        }

        // 2. 根据textbookId查询教材名称作为outputBaseName
        Textbook textbook = textbookMapper.selectById(textbookId);
        if (textbook == null) {
            return R.fail("错误：未找到ID为 " + textbookId + " 的教材信息。");
        }
        String outputBaseName = textbook.getTextbookName();
        // 对文件名进行基本清理，防止路径问题
        outputBaseName = outputBaseName.replaceAll("[\\\\/:*?\"<>|]", "_");

        Path tempHtmlPath = null;
        Path tempResourceDir = null;
        try {
            // 步骤 1: 生成原始的HTML文件
//            System.out.println("步骤 1: 开始从数据库生成HTML文件...");
            tempHtmlPath = generateTextbookHtml(textbookId);
//            System.out.println("-> HTML文件已生成在: " + tempHtmlPath.toAbsolutePath());

            // 步骤 2: 收集本地图片资源并重写HTML路径
//            System.out.println("步骤 2: 开始收集本地图片并重写HTML路径...");
            Path sourceImageDir = Paths.get(basePath + textbookId.toString()); // 源图片文件夹路径
            tempResourceDir = collectLocalResourcesAndRewriteHtml(tempHtmlPath, Paths.get(targetPath), sourceImageDir);
//            System.out.println("-> 图片已收集至: " + tempResourceDir.resolve(IMAGE_SUBFOLDER_NAME).toAbsolutePath());
//            System.out.println("-> HTML图片路径已更新为相对路径。");

            // 步骤 3: 将所有内容打包和编译
//            System.out.println("步骤 3: 开始执行加密打包和Go程序编译...");
            packageAndCompile(targetDeviceID, tempHtmlPath, tempResourceDir, outputBaseName, targetPath);

//            System.out.println("\n🎉🎉🎉 全部流程成功完成！ 🎉🎉🎉");

        } catch (Exception e) {
            System.err.println("❌ 处理失败！");
            e.printStackTrace();
            return R.fail("打包过程中发生错误: " + e.getMessage());
        } finally {
            // 步骤 4: 清理所有临时文件和文件夹
//            System.out.println("步骤 4: 开始清理临时文件...");
            if (tempHtmlPath != null) {
                try {
                    Files.deleteIfExists(tempHtmlPath);
//                    System.out.println("-> 临时HTML文件已清理: " + tempHtmlPath.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("警告：未能删除临时HTML文件: " + tempHtmlPath);
                }
            }
            if (tempResourceDir != null) {
                try {
                    deleteDirectoryRecursively(tempResourceDir);
//                    System.out.println("-> 临时资源文件夹已清理: " + tempResourceDir.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("警告：未能删除临时资源文件夹: " + tempResourceDir);
                }
            }
        }
        return R.ok("打包成功！请检查输出目录：" + targetPath);
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

    private Path generateTextbookHtml(Long textbookId) throws Exception {
        LambdaQueryWrapper<TextbookCatalog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TextbookCatalog::getTextbookId, textbookId);
        lambdaQueryWrapper.orderByAsc(TextbookCatalog::getSort);
        List<TextbookCatalog> textbookCatalogs = textbookCatalogMapper.selectList(lambdaQueryWrapper);

        if (textbookCatalogs.isEmpty()) {
            throw new RuntimeException("未找到ID为 " + textbookId + " 的教材内容。");
        }

        List<String> htmlFragments = textbookCatalogs.stream()
                .sorted(Comparator.comparing(TextbookCatalog::getSort))
                .flatMap(catalog -> Stream.of("<h2>" + catalog.getCatalogName() + "</h2>", catalog.getContent()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset='UTF-8'><title>教材内容</title></head><body>");
        for (String fragment : htmlFragments) {
            htmlBuilder.append(fragment).append("\n");
        }
        htmlBuilder.append("</body></html>");

        String mergedHtml = sanitizeHtml(htmlBuilder.toString());
        Path tempHtmlFile = Files.createTempFile("textbook_" + textbookId + "_", ".html");
        Files.write(tempHtmlFile, mergedHtml.getBytes(StandardCharsets.UTF_8));
        return tempHtmlFile;
    }

    private String sanitizeHtml(String html) {
        // 使用 Jsoup 清洗可能嵌套的 body/head 等结构
        org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(html);
        doc.outputSettings(new org.jsoup.nodes.Document.OutputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.html).escapeMode(Entities.EscapeMode.xhtml));
        return doc.html();
    }

    /**
     * 完整的打包和编译流程。MODIFIED: 接收动态的targetPath
     */
    public static void packageAndCompile(String deviceId, Path mainFile, Path resourceDir, String outputBaseName, String targetPath) throws Exception {
        String password = generateRandomPassword(12);
        System.out.println(" -> 1: 生成随机密码 -> " + password);

        Path zipOutputPath = Paths.get(targetPath, outputBaseName + ".zip");
        createEncryptedZip(mainFile, resourceDir, zipOutputPath, password);
        System.out.println(" -> 2: 创建加密ZIP包 -> " + zipOutputPath.toAbsolutePath());

        Path exeOutputPath = Paths.get(targetPath, outputBaseName + ".exe");
        compileGoExecutable(deviceId, password, exeOutputPath);
        System.out.println(" -> 3: 编译Go可执行文件 -> " + exeOutputPath.toAbsolutePath());
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
    private static void compileGoExecutable(String deviceId, String password, Path exeOutputPath) throws IOException, InterruptedException, URISyntaxException {
        // 定义工作区路径
        Path workspaceDir = Paths.get(GO_BUILD_WORKSPACE);
        if (!Files.isDirectory(workspaceDir)) {
            throw new IOException("Go编译工作区不存在，请先手动创建: " + workspaceDir);
        }

        URL resourceUrl = TextbookPackage.class.getResource("/template.go");
        if (resourceUrl == null) {
            throw new IOException("无法在类路径中找到资源文件: template.go");
        }

        Path templatePath;
        try {
            templatePath = Paths.get(resourceUrl.toURI());
        } catch (java.nio.file.FileSystemNotFoundException e) {
            java.nio.file.FileSystems.newFileSystem(resourceUrl.toURI(), java.util.Collections.emptyMap());
            templatePath = Paths.get(resourceUrl.toURI());
        }

        String templateContent = new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);
        String finalGoCode = templateContent
                .replace("DEVICE_CODE_PLACEHOLDER", deviceId)
                .replace("PASSWORD_PLACEHOLDER", password);

        Path tempGoFile = Files.createTempFile(workspaceDir, "unlocker_", ".go");
        Files.write(tempGoFile, finalGoCode.getBytes(StandardCharsets.UTF_8));

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