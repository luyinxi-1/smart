package com.upc.modular.common;

import com.aspose.words.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Word ⭢ HTML 多页拼接导出服务
 */
@Component
public class WordConversionPageService {

    /**
     * 按页转换 Word ⭢ HTML 并拼接输出为一个 HTML 文件
     *
     * @param inputWordPath  Word 文档路径
     * @param outputHtmlPath 输出 HTML 路径（拼接后的总文件）
     */
    public void convertWordToHtml(String inputWordPath, String outputHtmlPath) {
        System.out.println("Starting Word to Paged-HTML conversion...");
        System.out.println("Input file: " + inputWordPath);

        try {
            Document doc = new Document(inputWordPath);
            doc.updatePageLayout(); // 必须，启用分页逻辑

            int pageCount = doc.getPageCount();
            System.out.println("Total pages: " + pageCount);

            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta charset='UTF-8'></head><body>\n");

            for (int i = 0; i < pageCount; i++) {
                Document pageDoc = doc.extractPages(i, 1);

                HtmlSaveOptions options = new HtmlSaveOptions(SaveFormat.HTML);
                options.setExportImagesAsBase64(false);
                options.setExportFontsAsBase64(true);
                options.setOfficeMathOutputMode(HtmlOfficeMathOutputMode.IMAGE);
                options.setImagesFolder("D:\\workspace\\Files\\wordtohtml");
                options.setImagesFolderAlias("D:\\workspace\\Files\\wordtohtml");

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                pageDoc.save(out, options);
                String pageHtml = new String(out.toByteArray(), "UTF-8");

                // 提取 <body> 内部内容
                String bodyContent = pageHtml.replaceAll("(?s).*?<body.*?>(.*?)</body>.*", "$1");

                htmlBuilder.append("<div class='page' style='page-break-after:always;border-bottom:1px dashed #ccc;margin-bottom:30px;'>\n");
                htmlBuilder.append(bodyContent.trim()).append("\n</div>\n");
            }

            htmlBuilder.append("</body></html>");

            File outFile = new File(outputHtmlPath);
            outFile.getParentFile().mkdirs();
            Files.write(outFile.toPath(), htmlBuilder.toString().getBytes(StandardCharsets.UTF_8));

            System.out.println("✅ Paged HTML conversion completed!");
            System.out.println("Output saved at: " + outputHtmlPath);
        } catch (Exception e) {
            System.err.println("❌ Conversion failed!");
            e.printStackTrace();
        }
    }
}
