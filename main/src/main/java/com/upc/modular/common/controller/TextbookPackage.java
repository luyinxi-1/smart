package com.upc.modular.common.controller;

import com.upc.common.responseparam.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;

/**
 * @Author: xth
 * @Date: 2025/8/14 15:53
 */
@RestController
@RequestMapping("/TextbookPackage")
@Api(tags = "教材打包")
public class TextbookPackage {

    // 要压缩的文件路径
    private static String fileToZipPath = "C:\\Users\\yt\\Desktop\\test.txt";
    // go语言的指定工作区
    private static String GoBuildWorkspace = "D:\\Psoftware\\GoBuildWorkspace";
    // 目标位置
    private static String targetPath = "C:\\Users\\yt\\Desktop\\packageDemo\\";

    @ApiOperation(value = "教材打包")
    @PostMapping("/do")
    public R TextbookPackage(@RequestParam String targetDeviceID) {
        // 2. 要压缩的文件路径
        Path fileToZip = Paths.get(fileToZipPath);

        // 3. 最终输出的程序名称
        String outputBaseName = "MySecretTextbook";

        if ("YOUR_WINDOWS_UUID_HERE".equals(targetDeviceID)) {
            return R.ok("错误：请先设置targetDeviceID！");
        }

        try {
            // 执行打包和编译流程
            packageAndCompile(targetDeviceID, fileToZip, outputBaseName);
        } catch (Exception e) {
            System.err.println("处理失败！");
            e.printStackTrace();
        }

        return R.ok();
    }

    /**
     * 完整的打包和编译流程
     */
    public static void packageAndCompile(String deviceId, Path fileToZip, String outputBaseName) throws Exception {
        // 1. 生成一个随机的、安全的解压密码
        String password = generateRandomPassword(12);
        System.out.println("1: 生成随机密码 -> " + password);

        // 2. 将指定文件压缩并用生成的密码加密
        Path zipOutputPath = Paths.get( targetPath+ outputBaseName + ".zip");
        createEncryptedZip(fileToZip, zipOutputPath, password);
        System.out.println("2: 创建加密ZIP包 -> " + zipOutputPath.toAbsolutePath());

        // 3. 读取Go模板，替换占位符，并编译
        Path exeOutputPath = Paths.get(targetPath + outputBaseName + ".exe");
        compileGoExecutable(deviceId, password, exeOutputPath);
        System.out.println("3: 编译Go可执行文件 -> " + exeOutputPath.toAbsolutePath());

        System.out.println("\n全部完成！请检查" + targetPath + "目录。");
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
        Path workspaceDir = Paths.get(GoBuildWorkspace);
        if (!Files.isDirectory(workspaceDir)) {
            throw new IOException("Go编译工作区不存在，请先手动创建: " + workspaceDir);
        }

        // 读取Go模板文件
        URL resourceUrl = TextbookPackage.class.getResource("/template.go");
        if (resourceUrl == null) {
            throw new IOException("无法在类路径中找到资源文件: template.go");
        }
        URI resourceUri = resourceUrl.toURI();
        Path templatePath = Paths.get(resourceUri);
        String templateContent = new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);

        // 替换占位符
        String finalGoCode = templateContent
                .replace("DEVICE_CODE_PLACEHOLDER", deviceId)
                .replace("PASSWORD_PLACEHOLDER", password);

        Path tempGoFile = Files.createTempFile(workspaceDir, "unlocker_", ".go");
        Files.write(tempGoFile, finalGoCode.getBytes(StandardCharsets.UTF_8));

        // 准备并执行编译命令
        ProcessBuilder pb = new ProcessBuilder(
                "go", "build", "-ldflags=-H=windowsgui", "-o", exeOutputPath.toString(), tempGoFile.getFileName().toString() // 注意这里只用了文件名
        );

        pb.directory(workspaceDir.toFile());

        pb.inheritIO();

        System.out.println("... 开始编译Go程序，工作目录: " + workspaceDir + " ...");
        Process process = pb.start();
        int exitCode = process.waitFor();

        // 清理临时文件
        Files.delete(tempGoFile);

        if (exitCode != 0) {
            throw new RuntimeException("Go编译失败！请检查Go和C++编译器环境是否配置正确。");
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
