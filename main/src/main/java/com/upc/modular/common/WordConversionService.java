package com.upc.modular.common;

import com.aspose.words.Document;
import com.aspose.words.HtmlOfficeMathOutputMode;
import com.aspose.words.HtmlSaveOptions;
import com.aspose.words.SaveFormat;
import org.springframework.stereotype.Component;
import java.io.File;

/**
 * @Author: xth
 * @Date: 2025/7/11 20:34
 */
@Component
public class WordConversionService {
    /**
     * 将Word文档转换为HTML文件
     * @param inputWordPath 输入的Word文档完整路径
     * @param outputHtmlPath 输出的HTML文件完整路径
     */
    public void convertWordToHtml(String inputWordPath, String outputHtmlPath) {
        System.out.println("Starting Word to HTML conversion...");
        System.out.println("Input file: " + inputWordPath);

        try {
            // 1. 加载Word文档
            Document doc = new Document(inputWordPath);

            // 2. 创建并配置HTML保存选项
            HtmlSaveOptions saveOptions = new HtmlSaveOptions();
            saveOptions.setSaveFormat(SaveFormat.HTML); // 设置保存格式为HTML

            // --- 这是关键配置 ---
            // A. 设置公式（OfficeMath）的输出模式为图片（IMAGE）
            saveOptions.setOfficeMathOutputMode(HtmlOfficeMathOutputMode.IMAGE);

            // B. 将图片以Base64编码的形式内嵌到HTML文件中，生成单文件
            // saveOptions.setExportImagesAsBase64(true);
            saveOptions.setExportImagesAsBase64(false);
            saveOptions.setImagesFolder("D:\\workspace\\Files\\wordtohtml");               // 实际磁盘文件夹
            saveOptions.setImagesFolderAlias("D:\\workspace\\Files\\wordtohtml");          // HTML 内部引用路径

            // C. 将字体以内嵌的形式保存，保证在不同电脑上显示效果一致
            saveOptions.setExportFontsAsBase64(true);

            // 确保输出目录存在
            File outputFile = new File(outputHtmlPath);
            outputFile.getParentFile().mkdirs();

            // 3. 保存为HTML文件
            doc.save(outputHtmlPath, saveOptions);

            System.out.println("Conversion successful!");
            System.out.println("Output file saved to: " + outputHtmlPath);

        } catch (Exception e) {
            System.err.println("An error occurred during conversion.");
            e.printStackTrace();
        }
    }
}
