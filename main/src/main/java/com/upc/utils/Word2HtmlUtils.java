package com.upc.utils;

import com.aspose.words.Document;
import com.aspose.words.HtmlOfficeMathOutputMode;
import com.aspose.words.HtmlSaveOptions;
import com.aspose.words.SaveFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * <h2>Word ⭢ HTML 全场景工具类</h2>
 * <p>
 * 已支持 7 种常用转换通道：
 * <pre>
 * 1. Path            → Path      convert(Path)
 * 2. File            → Path      convert(File)
 * 3. File            → File      convertReturnFile(File)
 * 4. MultipartFile   → Path      toHtmlFile(MultipartFile)
 * 5. MultipartFile   → String    toHtmlString(MultipartFile)
 * 6. File            → String    toHtmlString(File)   (新增)
 * 7. Path            → String    toHtmlString(Path)   (新增)
 * </pre>
 * 所有接口均生成 <strong>单文件 HTML</strong>（图片、字体 base64 内嵌）。
 * <p>
 * Aspose.Words 为商业依赖，请自行通过 Maven/Gradle 引入。
 */
@Component
public final class Word2HtmlUtils {

    private static String basePath;
    private static String serverurl;

    @Value("${files.path}")
    public void setBasePath(String path) {
        Word2HtmlUtils.basePath = path + "/convertTextbookImage";
    }

    @Value("${server.baseurl}")
    public void setServerurl(String serverurl) {
        Word2HtmlUtils.serverurl = serverurl;
    }


    private Word2HtmlUtils() {}

    /* --------------------------- 公共 API --------------------------- */

    /** Path → Path */
    public static Path convert(Path wordPath) throws Exception {
        Objects.requireNonNull(wordPath, "wordPath must not be null");
        if (!Files.isRegularFile(wordPath)) {
            throw new IllegalArgumentException("Not a regular file: " + wordPath);
        }
        return convertInternal(wordPath.toFile(), null);
    }

    /** File → Path */
    public static Path convert(File wordFile) throws Exception {
        Objects.requireNonNull(wordFile, "wordFile must not be null");
        return convertInternal(wordFile, null);
    }

    /** File → File（返回完整 File 对象） */
    public static File convertReturnFile(File wordFile) throws Exception {
        return convertInternal(wordFile, null).toFile();
    }

    /** MultipartFile → Path（自动落盘到临时目录） */
    public static Path toHtmlFile(MultipartFile upload) throws Exception {
        Objects.requireNonNull(upload, "multipartFile must not be null");
        Path tempWord = Files.createTempFile("upload_", getFileSuffix(upload.getOriginalFilename()));
        upload.transferTo(tempWord);
        return convertInternal(tempWord.toFile(), null);
    }

    /** MultipartFile → HTML String（纯内存回显 / 推送） */
    public static String toHtmlString(MultipartFile upload, Long textbookId) throws Exception {
        Objects.requireNonNull(upload, "multipartFile must not be null");
        try (InputStream in = upload.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document doc = new Document(in);
            HtmlSaveOptions opt = buildHtmlSaveOptions(textbookId);
            doc.save(out, opt);
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    /** File → HTML String （新增：不落盘，直接返回源码） */
    public static String toHtmlString(File wordFile) throws Exception {
        Objects.requireNonNull(wordFile, "wordFile must not be null");
        if (!wordFile.exists()) {
            throw new FileNotFoundException("Word file not found: " + wordFile);
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(wordFile.getAbsolutePath());
            HtmlSaveOptions opt = buildHtmlSaveOptions(null);
            doc.save(out, opt);
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    /** Path → HTML String （新增：复用 File 版本） */
    public static String toHtmlString(Path wordPath) throws Exception {
        Objects.requireNonNull(wordPath, "wordPath must not be null");
        if (!Files.isRegularFile(wordPath)) {
            throw new IllegalArgumentException("Not a regular file: " + wordPath);
        }
        return toHtmlString(wordPath.toFile());
    }

    /* ----------------------- 内部核心实现 --------------------------- */

    /**
     * 真正执行 Word→HTML 转换；若 outputDir 为 null，则使用系统临时目录。
     */
    private static Path convertInternal(File wordFile, Path outputDir) throws Exception {
        if (!wordFile.exists()) {
            throw new FileNotFoundException("Word file not found: " + wordFile);
        }

        if (outputDir == null) {
            outputDir = Files.createTempDirectory("word2html_");
        }

        String baseName = getBaseName(wordFile.getName());
        Path htmlPath = outputDir.resolve(baseName + ".html");

        Document doc = new Document(wordFile.getAbsolutePath());
        HtmlSaveOptions opt = buildHtmlSaveOptions(null);
        doc.save(htmlPath.toString(), opt);

        return htmlPath;
    }

    /* -------------------- 公共配置构造 ------------------------ */

    private static HtmlSaveOptions buildHtmlSaveOptions(Long textbookId) {
        HtmlSaveOptions opt = new HtmlSaveOptions(SaveFormat.HTML);
        opt.setOfficeMathOutputMode(HtmlOfficeMathOutputMode.IMAGE);
//        opt.setExportImagesAsBase64(true);
//        opt.setExportFontsAsBase64(true);
        opt.setExportImagesAsBase64(false);
        String imagesHttpPath = serverurl + "/" + basePath + "/" + textbookId;
        opt.setImagesFolder(basePath + "/" + textbookId);   // 实际磁盘文件夹
        opt.setImagesFolderAlias(imagesHttpPath);           // HTML 内部引用路径
        opt.setPrettyFormat(true);
        return opt;
    }

    /* ------------------------ 小工具方法 ----------------------------- */

    private static String getBaseName(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot == -1 ? filename : filename.substring(0, dot));
    }

    private static String getFileSuffix(String filename) {
        if (filename == null) return ".docx";
        int dot = filename.lastIndexOf('.');
        return (dot == -1 ? ".docx" : filename.substring(dot));
    }
}
