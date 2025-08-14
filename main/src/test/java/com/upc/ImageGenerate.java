package com.upc;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 这是一个独立的Java程序，用于生成占位符图片。
 * 运行 main 方法即可。
 */
public class ImageGenerate {

    /**
     * 主程序入口 - 运行此方法即可
     */
    public static void main(String[] args) {
        // ==================== 配置区 ====================

        // 1. 设置图片要保存到的目标文件夹路径
        // 注意：Java中的反斜杠 \ 需要写成 \\
        String outputDirectory = "D:\\QQ\\861888034\\FileRecv\\研究生\\教材采购系统\\代码类\\image";

        // 2. 图片尺寸
        int imageWidth = 200;
        int imageHeight = 150;

        // 3. 需要生成的图片文件名列表 (已自动去重)
        String[] filenamesArray = {
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.001.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.002.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.003.jpeg", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.004.jpeg",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.005.jpeg", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.006.jpeg",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.007.jpeg", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.008.jpeg",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.009.jpeg", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.010.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.011.jpeg", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.012.jpeg",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.013.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.014.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.015.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.016.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.017.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.018.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.019.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.020.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.021.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.022.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.023.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.024.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.025.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.026.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.027.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.028.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.029.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.030.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.031.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.032.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.033.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.034.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.035.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.036.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.037.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.038.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.039.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.040.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.041.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.042.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.043.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.044.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.045.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.046.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.047.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.048.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.049.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.050.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.051.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.052.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.053.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.054.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.055.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.056.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.057.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.058.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.059.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.060.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.061.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.062.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.063.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.064.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.065.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.066.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.067.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.068.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.069.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.070.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.071.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.072.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.073.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.074.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.075.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.076.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.077.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.078.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.079.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.080.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.081.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.082.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.083.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.084.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.085.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.086.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.087.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.088.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.089.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.090.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.091.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.092.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.093.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.094.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.095.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.096.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.097.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.098.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.099.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.100.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.101.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.102.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.103.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.104.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.105.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.106.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.107.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.108.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.109.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.110.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.111.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.112.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.113.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.114.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.115.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.116.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.117.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.118.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.119.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.120.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.121.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.122.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.123.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.124.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.125.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.126.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.127.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.128.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.129.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.130.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.131.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.132.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.133.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.134.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.135.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.136.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.137.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.138.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.139.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.140.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.141.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.142.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.143.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.144.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.145.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.146.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.147.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.148.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.149.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.150.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.151.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.152.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.153.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.154.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.155.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.156.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.157.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.158.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.159.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.160.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.161.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.162.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.163.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.164.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.165.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.166.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.167.png", "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.168.png",
                "Aspose.Words.830ee488-d05a-4bf4-a65e-ee607494d39e.169.png"
        };

        // ==================== 执行区 ====================

        // 使用 Set 去除重复的文件名
        Set<String> uniqueFilenames = new HashSet<>(Arrays.asList(filenamesArray));

        // 确保目标文件夹存在
        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            System.out.println("目标文件夹不存在, 正在创建: " + dir.getAbsolutePath());
            boolean created = dir.mkdirs();
            if (!created) {
                System.err.println("错误: 创建文件夹失败！请检查路径和权限。");
                return;
            }
        }

        System.out.println("开始生成 " + uniqueFilenames.size() + " 张占位图片...");

        for (String filename : uniqueFilenames) {
            try {
                File outputFile = Paths.get(outputDirectory, filename).toFile();
                createPlaceholderImage(outputFile, filename, imageWidth, imageHeight);
                System.out.println("  [成功] " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("  [失败] " + filename + " - " + e.getMessage());
            }
        }

        System.out.println("\n全部完成！请检查文件夹: " + outputDirectory);
    }

    /**
     * 创建一个带有文字的灰色占位图
     * @param outputFile 目标图片文件
     * @param text       要绘制在图片上的文字
     * @param width      图片宽度
     * @param height     图片高度
     * @throws IOException 如果写入文件失败
     */
    public static void createPlaceholderImage(File outputFile, String text, int width, int height) throws IOException {
        String fileName = outputFile.getName();
        String extension = "png"; // 默认格式
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex + 1);
        }
        // ImageIO 对 jpeg 格式的官方名称是 "jpg"
        if ("jpeg".equalsIgnoreCase(extension)) {
            extension = "jpg";
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 绘制灰色背景
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, width, height);

        // 绘制深灰色边框
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(0, 0, width - 1, height - 1);

        // 绘制文字 (文件名)
        g2d.setColor(Color.BLACK);
        // 动态调整字体大小以适应图片宽度
        Font font = new Font("Arial", Font.PLAIN, 12);
        FontMetrics fm = g2d.getFontMetrics(font);
        // 如果文字太长，缩小字体
        while (fm.stringWidth(text) > width - 20) {
            font = new Font(font.getName(), font.getStyle(), font.getSize() - 1);
            fm = g2d.getFontMetrics(font);
        }
        g2d.setFont(font);

        // 将文字居中
        int textX = (width - fm.stringWidth(text)) / 2;
        int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, textX, textY);

        // 释放资源
        g2d.dispose();

        // 将内存中的图片写入到文件
        ImageIO.write(image, extension, outputFile);
    }
}
