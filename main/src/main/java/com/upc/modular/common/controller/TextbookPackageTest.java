package com.upc.modular.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.TextbookCatalog;
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
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: xth
 * @Date: 2025/8/14 15:53
 */
@RestController
@RequestMapping("/TextbookPackageTest")
@Api(tags = "教材打包测试")
public class TextbookPackageTest {

    @Autowired
    private TextbookCatalogMapper textbookCatalogMapper;

    // go语言的指定工作区
    private static final String GO_BUILD_WORKSPACE = "D:\\build_workspace";
    // 最终打包文件的输出目录
    private static final String TARGET_PATH = "C:\\Users\\luyinxi\\OneDrive\\Desktop\\";
    // 在HTML中引用的图片相对路径文件夹名称
    private static final String RELATIVE_IMAGE_FOLDER = "D:\\QQ\\861888034\\FileRecv\\研究生\\教材采购系统\\代码类\\image";

    @ApiOperation(value = "教材打包")
    @PostMapping("/do")
    public R textbookPackage(@RequestParam String targetDeviceID, @RequestParam Long textbookId) {
        // 最终输出的程序基础名称 (例如 MySecretTextbook.exe, MySecretTextbook.zip)
        String outputBaseName = "MySecretTextbook";

        if (targetDeviceID == null || targetDeviceID.isEmpty() || "YOUR_WINDOWS_UUID_HERE".equals(targetDeviceID)) {
            return R.fail("错误：请提供一个有效的目标设备ID (targetDeviceID)！");
        }

        Path tempHtmlPath = null;
        try {
            // 1. 根据 textbookId 从数据库生成教材的 HTML 文件
            System.out.println("步骤 1: 开始从数据库生成HTML文件...");
            tempHtmlPath = generateTextbookHtml(textbookId);
            System.out.println("-> HTML文件已生成在: " + tempHtmlPath.toAbsolutePath());

            // 2. 修改HTML文件中的图片路径为相对路径
            System.out.println("步骤 2: 开始修改HTML文件中的图片为相对路径...");
            modifyImagePathsToRelative(tempHtmlPath, RELATIVE_IMAGE_FOLDER);
            System.out.println("-> 图片路径已修改，将引用 '" + RELATIVE_IMAGE_FOLDER + "/' 文件夹。");

            // 3. 执行完整的打包和编译流程
            System.out.println("步骤 3: 开始执行加密打包和Go程序编译...");
            packageAndCompile(targetDeviceID, tempHtmlPath, outputBaseName);

            System.out.println("\n🎉🎉🎉 全部流程成功完成！ 🎉🎉🎉");

        } catch (Exception e) {
            System.err.println("❌ 处理失败！");
            e.printStackTrace();
            // 修改点2: 将 R.error 替换为 R.fail
            return R.fail("打包过程中发生错误: " + e.getMessage());
        }
//        finally {
//            // 4. 清理临时生成的HTML文件
//            if (tempHtmlPath != null) {
//                try {
//                    Files.delete(tempHtmlPath);
//                    System.out.println("-> 临时HTML文件已清理: " + tempHtmlPath.toAbsolutePath());
//                } catch (IOException e) {
//                    System.err.println("警告：未能删除临时HTML文件: " + tempHtmlPath);
//                    e.printStackTrace();
//                }
//            }
//        }

        return R.ok("打包成功！请检查输出目录：" + TARGET_PATH);
    }

    /**
     * 根据教材ID从数据库生成一个临时的HTML文件
     *
     * @param textbookId 教材ID
     * @return 生成的临时HTML文件的路径
     * @throws Exception 如果数据库查询或文件写入失败
     */
    private Path generateTextbookHtml(Long textbookId) throws Exception {
        // 查询数据库获取教材目录和内容
        LambdaQueryWrapper<TextbookCatalog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TextbookCatalog::getTextbookId, textbookId);
        lambdaQueryWrapper.orderByAsc(TextbookCatalog::getSort);
        List<TextbookCatalog> textbookCatalogs = textbookCatalogMapper.selectList(lambdaQueryWrapper);

        if (textbookCatalogs.isEmpty()) {
            throw new RuntimeException("未找到ID为 " + textbookId + " 的教材内容。");
        }

        // 将目录名和内容流式处理，过滤掉null值
        List<String> htmlFragments = textbookCatalogs.stream()
                .sorted(Comparator.comparing(TextbookCatalog::getSort))
                .flatMap(catalog -> Stream.of(
                        // 为目录标题添加一个h2标签使其更醒目
                        "<h2>" + catalog.getCatalogName() + "</h2>",
                        catalog.getContent()
                ))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 拼接成完整的HTML结构
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset='UTF-8'><title>教材内容</title></head><body>");
        for (String fragment : htmlFragments) {
            htmlBuilder.append(fragment).append("\n");
        }
        htmlBuilder.append("</body></html>");

        // 使用Jsoup清洗和规范化HTML，防止无效的嵌套结构
        String mergedHtml = sanitizeHtml(htmlBuilder.toString());

        // 创建一个临时文件来保存HTML内容
        Path tempHtmlFile = Files.createTempFile("textbook_" + textbookId + "_", ".html");
        Files.write(tempHtmlFile, mergedHtml.getBytes(StandardCharsets.UTF_8));

        return tempHtmlFile;
    }

    /**
     * 读取HTML文件，将其中的所有图片src属性修改为相对路径
     *
     * @param htmlFile          要修改的HTML文件路径
     * @param relativeImageFolder 目标相对路径的文件夹名 (例如 "images")
     * @throws IOException 如果文件读写失败
     */
    private void modifyImagePathsToRelative(Path htmlFile, String relativeImageFolder) throws IOException {
        String htmlContent = new String(Files.readAllBytes(htmlFile), StandardCharsets.UTF_8);
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);

        Elements images = doc.select("img");
        for (Element img : images) {
            String originalSrc = img.attr("src");
            if (originalSrc != null && !originalSrc.isEmpty()) {
                String fileName = Paths.get(originalSrc).getFileName().toString();

                // 拼接新的相对路径
                String newSrc = relativeImageFolder + "/" + fileName;
                img.attr("src", newSrc);
                System.out.println("  - 图片路径转换: '" + originalSrc + "' -> '" + newSrc + "'");
            }
        }

        // 将修改后的HTML内容写回原文件
        Files.write(htmlFile, doc.outerHtml().getBytes(StandardCharsets.UTF_8));
    }


    private String sanitizeHtml(String html) {
        // 使用 Jsoup 清洗可能嵌套的 body/head 等结构
        org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(html);
        doc.outputSettings(new org.jsoup.nodes.Document.OutputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.html).escapeMode(Entities.EscapeMode.xhtml));
        return doc.html();
    }
    /**
     * 完整的打包和编译流程
     */
    public static void packageAndCompile(String deviceId, Path fileToZip, String outputBaseName) throws Exception {
        // 1. 生成一个随机的、安全的解压密码
        String password = generateRandomPassword(12);
        System.out.println(" -> 1: 生成随机密码 -> " + password);

        // 2. 将指定文件压缩并用生成的密码加密
        Path zipOutputPath = Paths.get( TARGET_PATH + outputBaseName + ".zip");
        createEncryptedZip(fileToZip, zipOutputPath, password);
        System.out.println(" -> 2: 创建加密ZIP包 -> " + zipOutputPath.toAbsolutePath());

        // 3. 读取Go模板，替换占位符，并编译
        Path exeOutputPath = Paths.get(TARGET_PATH + outputBaseName + ".exe");
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
    private static void createEncryptedZip(Path fileToZip, Path zipOutputPath, String password) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipOutputPath.toFile(), password.toCharArray())) {
            ZipParameters params = new ZipParameters();
            params.setEncryptFiles(true);
            params.setEncryptionMethod(EncryptionMethod.AES);
            zipFile.addFile(fileToZip.toFile(), params);
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

        // 读取Go模板文件
        URL resourceUrl = TextbookPackage.class.getResource("/template.go");
        if (resourceUrl == null) {
            throw new IOException("无法在类路径中找到资源文件: template.go");
        }

        // 兼容JAR包和文件系统环境的路径读取方式
        Path templatePath;
        try {
            templatePath = Paths.get(resourceUrl.toURI());
        } catch (java.nio.file.FileSystemNotFoundException e) {
            // 如果在JAR包中，需要特殊处理
            java.nio.file.FileSystems.newFileSystem(resourceUrl.toURI(), java.util.Collections.emptyMap());
            templatePath = Paths.get(resourceUrl.toURI());
        }

        String templateContent = new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);

        // 替换占位符
        String finalGoCode = templateContent
                .replace("DEVICE_CODE_PLACEHOLDER", deviceId)
                .replace("PASSWORD_PLACEHOLDER", password);

        Path tempGoFile = Files.createTempFile(workspaceDir, "unlocker_", ".go");
        Files.write(tempGoFile, finalGoCode.getBytes(StandardCharsets.UTF_8));

        // 准备并执行编译命令
        ProcessBuilder pb = new ProcessBuilder(
                "go", "build", "-ldflags=-H=windowsgui", "-o", exeOutputPath.toString(), tempGoFile.getFileName().toString()
        );

        pb.directory(workspaceDir.toFile());
        pb.inheritIO();

        System.out.println(" ... 开始编译Go程序，工作目录: " + workspaceDir + " ...");
        Process process = pb.start();
        int exitCode = process.waitFor();

        // 清理临时文件
        Files.delete(tempGoFile);

        if (exitCode != 0) {
            throw new RuntimeException("Go编译失败！请检查Go和C++编译器环境是否配置正确，以及Go命令是否在系统PATH中。");
        }
    }
}