package com.upc.modular.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.controller.param.vo.ApplicationMaterialsVO;
import com.upc.modular.materials.entity.MaterialsTextbookMapping;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.service.IMaterialsTextbookMappingService;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.entity.TextbookClassification;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;

import com.upc.modular.textbook.service.ITextbookCatalogService;
import com.upc.modular.textbook.service.ITextbookService;
import com.upc.modular.textbook.service.ITextbookClassificationService;
import com.upc.modular.textbook.param.ReadTextbookReturnParam;
import com.upc.modular.textbook.param.TextbookPageReturnParam;

// 添加应用资源相关导入
import com.upc.modular.materials.entity.ApplicationMaterials;
import com.upc.modular.materials.entity.ApplicationMaterialsTextbookMapping;
import com.upc.modular.materials.service.IApplicationMaterialsService;
import com.upc.modular.materials.service.IApplicationMaterialsMappingService;
import com.upc.modular.materials.service.IApplicationMaterialsTextbookMappingService;
import com.upc.modular.materials.controller.param.vo.ApplicationMaterialsDetailVO;

// 添加资料推送相关导入
import com.upc.modular.textbook.entity.MaterialList;
import com.upc.modular.textbook.entity.MaterialPush;
import com.upc.modular.textbook.service.IMaterialPushService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.nio.charset.StandardCharsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.OutputStream;

@RestController
@RequestMapping("/TextbookPackage")
@Api(tags = "教材打包 (含资源版)")
public class TextbookPackage {

    @Autowired
    private TextbookCatalogMapper textbookCatalogMapper;
    @Autowired
    private TextbookMapper textbookMapper;
    // 添加新的服务依赖
    @Autowired
    private ITextbookCatalogService textbookCatalogService;
    @Autowired
    private ITextbookService textbookService;
    @Autowired
    private ITextbookClassificationService textbookClassificationService;
    @Autowired
    private IMaterialsTextbookMappingService materialsTextbookMappingService;
    @Autowired
    private ITeachingMaterialsService teachingMaterialsService;
    
    // 添加应用资源相关服务依赖
    @Autowired
    private IApplicationMaterialsService applicationMaterialsService;
    @Autowired
    private IApplicationMaterialsMappingService applicationMaterialsMappingService;
    @Autowired
    private IApplicationMaterialsTextbookMappingService applicationMaterialsTextbookMappingService;

    // 添加资料推送相关服务依赖
    @Autowired
    private IMaterialPushService materialPushService;
    @Autowired
    private TeacherMapper teacherMapper;

    // Go 语言编译的工作区
//    private static final String GO_BUILD_WORKSPACE = "/opt/GoBuildWorkspace";
    // 临时改成这个，部署到linux之前得改回来。
    private static final String GO_BUILD_WORKSPACE = "D:/GoBuildWorkspace";

    // *** UPDATED ***: Base path for textbook pictures updated to the specific server path.
//    private static final String TEXTBOOK_PICTURE_BASE_PATH = "/opt/textbook-app/upload/public/picture/convertTextbookImage/";
    private static final String TEXTBOOK_PICTURE_BASE_PATH = "D:/textbook-app/upload/public/picture/convertTextbookImage/";

    // 教学素材文件基础路径
//    private static final String TEACHING_MATERIALS_BASE_PATH = "/opt/textbook-app/";
    private static final String TEACHING_MATERIALS_BASE_PATH = "D:/textbook-app/";

    @ApiOperation(value = "教材打包（包含图片等资源）")
    @PostMapping("/do")
    public void textbookPackage(@RequestParam String targetDeviceID,
                               @RequestParam Long textbookId,
                               HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        
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

            // 3. 从数据库获取内容, 复制资源并生成一个临时的HTML文件
            long htmlStartTime = System.currentTimeMillis();
            Path textbookHtmlPath = generateTextbookHtmlWithResources(textbookId, temporaryWorkDir, outputBaseName);
            long htmlEndTime = System.currentTimeMillis();

            // 4. 打包HTML及资源文件、编译Go解锁程序，并生成最终的包
            long packageStartTime = System.currentTimeMillis();
            Path finalPackagePath = packageAndCompile(targetDeviceID, textbookHtmlPath, temporaryWorkDir, outputBaseName);
            long packageEndTime = System.currentTimeMillis();

            // 5. 直接将文件流写入HTTP响应
            File zipFile = finalPackagePath.toFile();
            String fileName = outputBaseName + "_Package.zip";
            String encodedFileName = java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                                      .replaceAll("\\+", "%20");
            String fallbackName = fileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s", fallbackName, encodedFileName));
            response.setContentLengthLong(zipFile.length());

            try (InputStream in = new FileInputStream(zipFile);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }

            long endTime = System.currentTimeMillis();

        } catch (Exception e) {
            System.err.println("教材打包失败！Textbook ID: " + textbookId);
            System.err.println("异常类型: " + e.getClass().getName());
            System.err.println("异常信息: " + e.getMessage());
            e.printStackTrace();
            // 出现异常时返回错误信息
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "教材打包失败");
        } finally {
            // 6. 清理服务器上的临时目录和文件
            if (temporaryWorkDir != null) {
                try {
                    deleteDirectoryRecursively(temporaryWorkDir);
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
        System.out.println("  -> 查询数据库获取章节内容...");
        // 查询数据库获取章节内容
        LambdaQueryWrapper<TextbookCatalog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookCatalog::getTextbookId, textbookId);
        queryWrapper.orderByAsc(TextbookCatalog::getSort);
        List<TextbookCatalog> textbookCatalogs = textbookCatalogMapper.selectList(queryWrapper);
        System.out.println("  -> 共获取到 " + textbookCatalogs.size() + " 个章节");
        
        // 记录前几个章节的信息用于调试
        if (!textbookCatalogs.isEmpty()) {
            System.out.println("  -> 前3个章节ID: " + 
                             textbookCatalogs.stream()
                                           .limit(3)
                                           .map(c -> String.valueOf(c.getId()))
                                           .collect(java.util.stream.Collectors.joining(", ")));
        }

        if (textbookCatalogs.isEmpty()) {
            throw new IOException("数据库中未找到ID为 " + textbookId + " 的教材内容。");
        }

        // 拼接成一个完整的HTML文档
        System.out.println("  -> 拼接HTML内容...");
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
            System.out.println("  -> 发现资源目录: " + sourceImageDir);
            Files.createDirectories(targetImgDir); // 创建 resource/img 文件夹

            // 复制整个图片目录到临时工作区的 resource/img 下
            System.out.println("  -> 开始复制资源文件...");
            long copyStartTime = System.currentTimeMillis();
            int fileCount = 0;
            int totalFiles = 0;
            
            try (Stream<Path> stream = Files.walk(sourceImageDir)) {
                // 先计算总文件数
                totalFiles = (int) stream.filter(Files::isRegularFile).count();
            }
            
            System.out.println("  -> 发现 " + totalFiles + " 个文件需要复制");
            
            try (Stream<Path> stream = Files.walk(sourceImageDir)) {
                // 使用AtomicInteger来安全地在lambda表达式中更新计数器
                final java.util.concurrent.atomic.AtomicInteger copiedFiles = new java.util.concurrent.atomic.AtomicInteger(0);
                final int finalTotalFiles = totalFiles; // 创建final副本供lambda表达式使用
                fileCount = (int) stream.map(source -> {
                    try {
                        if (Files.isRegularFile(source)) {
                            Path destination = targetImgDir.resolve(sourceImageDir.relativize(source));
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                            int currentCount = copiedFiles.incrementAndGet();
                            
                            // 每复制100个文件打印一次进度
                            if (currentCount % 100 == 0) {
                                System.out.println("  -> 已复制 " + currentCount + " / " + finalTotalFiles + " 个文件");
                            }
                        }
                        return source;
                    } catch (IOException e) {
                        System.err.println("警告：复制文件失败 " + source + " -> " + e.getMessage());
                        return null;
                    }
                }).count();
            }
            
            long copyEndTime = System.currentTimeMillis();
            System.out.println("  -> 资源文件复制完成，共复制 " + fileCount + " 个文件，耗时: " + (copyEndTime - copyStartTime) + "ms");

            // 3. 使用Jsoup解析HTML并修改图片路径为相对路径
            System.out.println("  -> 更新HTML中的图片路径...");
            long htmlProcessStartTime = System.currentTimeMillis();
            Document doc = Jsoup.parse(finalHtmlContent);
            Elements images = doc.select("img");
            System.out.println("  -> 共找到 " + images.size() + " 个图片标签");
            
            int processedImages = 0;
            for (Element img : images) {
                String src = img.attr("src");
                // 从绝对路径中提取文件名
                String fileName = new File(src).getName();
                // 更新为新的相对路径
                img.attr("src", "./resource/img/" + fileName);
                
                processedImages++;
                if (processedImages % 1000 == 0) {
                    System.out.println("  -> 已处理 " + processedImages + " / " + images.size() + " 个图片标签");
                }
            }
            
            finalHtmlContent = doc.html();
            long htmlProcessEndTime = System.currentTimeMillis();
            System.out.println("  -> HTML图片路径更新完成, 耗时: " + (htmlProcessEndTime - htmlProcessStartTime) + "ms");
        } else {
            System.out.println("  -> 未找到资源目录: " + sourceImageDir + ", 将直接打包纯文本HTML。");
        }

        // 4. 在指定工作目录中创建最终的HTML文件
        Path htmlFilePath = workDir.resolve(outputBaseName + ".html");
        Files.write(htmlFilePath, finalHtmlContent.getBytes(StandardCharsets.UTF_8));
        System.out.println("  -> 已生成HTML文件: " + htmlFilePath);
        
        // 5. 添加生成JS数据文件的调用
        System.out.println("  -> 开始生成JS数据文件...");
        long jsStartTime = System.currentTimeMillis();
        generateBookDataJs(textbookId, workDir);
        long jsEndTime = System.currentTimeMillis();
        System.out.println("  -> JS数据文件生成完成，耗时: " + (jsEndTime - jsStartTime) + "ms");
        
        // 6. 添加教学素材处理
        System.out.println("  -> 开始处理教学素材...");
        long materialsStartTime = System.currentTimeMillis();
        Map<String, Map<String, String>> resourceMap = processTeachingMaterials(textbookId, workDir);
        long materialsEndTime = System.currentTimeMillis();
        System.out.println("  -> 教学素材处理完成，耗时: " + (materialsEndTime - materialsStartTime) + "ms");
        
        // 7. 添加应用素材处理
        System.out.println("  -> 开始处理应用素材...");
        long appMaterialsStartTime = System.currentTimeMillis();
        processApplicationMaterials(textbookId, workDir, resourceMap);
        long appMaterialsEndTime = System.currentTimeMillis();
        System.out.println("  -> 应用素材处理完成，耗时: " + (appMaterialsEndTime - appMaterialsStartTime) + "ms");
        
        // 8. 生成resourceMap.js文件
        generateResourceMapJs(resourceMap, workDir);
        
        // 9. 添加资料推送处理
        System.out.println("  -> 开始处理资料推送...");
        long materialPushStartTime = System.currentTimeMillis();
        processMaterialPushes(textbookId, workDir, resourceMap);
        long materialPushEndTime = System.currentTimeMillis();
        System.out.println("  -> 资料推送处理完成，耗时: " + (materialPushEndTime - materialPushStartTime) + "ms");
        
        // 10. 重新生成resourceMap.js文件（包含资料推送）
        generateResourceMapJs(resourceMap, workDir);
        
        // 11. 复制index.html文件到工作目录
        System.out.println("  -> 开始复制index.html文件...");
        long indexHtmlStartTime = System.currentTimeMillis();
        copyIndexHtmlToWorkDir(workDir);
        long indexHtmlEndTime = System.currentTimeMillis();
        System.out.println("  -> index.html文件复制完成，耗时: " + (indexHtmlEndTime - indexHtmlStartTime) + "ms");

        return htmlFilePath;
    }

    /**
     * 复制index.html文件到工作目录
     * 
     * @param workDir 工作目录
     * @throws IOException 文件操作异常
     */
    public static void copyIndexHtmlToWorkDir(Path workDir) throws IOException {
        // 获取resources/static/index.html文件的输入流
        InputStream indexHtmlStream = TextbookPackage.class.getClassLoader()
                .getResourceAsStream("static/index.html");
        
        if (indexHtmlStream == null) {
            System.out.println("    --> 警告：未找到resources/static/index.html文件");
            return;
        }
        
        // 定义目标文件路径
        Path targetIndexPath = workDir.resolve("index.html");
        System.out.println("    --> 目标文件路径: " + targetIndexPath);
        
        // 复制文件
        try {
            Files.copy(
                indexHtmlStream, 
                targetIndexPath, 
                StandardCopyOption.REPLACE_EXISTING
            );
            System.out.println("    --> index.html文件复制完成");
        } catch (IOException e) {
            System.err.println("    --> 复制index.html文件失败: " + e.getMessage());
            throw e;
        } finally {
            try {
                indexHtmlStream.close();
            } catch (IOException e) {
                System.err.println("    --> 关闭index.html输入流失败: " + e.getMessage());
            }
        }
    }

    /**
     * 生成教材JS数据文件(bookData.js)
     * 包含教材章节数据和教材信息
     * 
     * @param textbookId 教材ID
     * @param workDir 工作目录
     * @throws IOException 文件操作异常
     */
    private void generateBookDataJs(Long textbookId, Path workDir) throws IOException {
        System.out.println("    --> 调用readTextbook接口获取章节数据...");
        // 1. 调用readTextbook接口获取章节数据
        long apiCallStartTime = System.currentTimeMillis();
        List<ReadTextbookReturnParam> catalogList = textbookCatalogService.readTextbook(textbookId);
        long apiCallEndTime = System.currentTimeMillis();
        System.out.println("    --> 共获取到 " + catalogList.size() + " 个章节数据, API调用耗时: " + (apiCallEndTime - apiCallStartTime) + "ms");
        
        // 2. 处理章节数据中的资源链接，确保与HTML中的一致
        Path sourceImageDir = Paths.get(TEXTBOOK_PICTURE_BASE_PATH, String.valueOf(textbookId));
        if (Files.exists(sourceImageDir) && Files.isDirectory(sourceImageDir)) {
            System.out.println("    --> 处理章节数据中的资源链接...");
            // 对章节数据应用与HTML相同的资源链接处理
            processResourceLinksInCatalogList(catalogList, textbookId);
            System.out.println("    --> 资源链接处理完成");
        }
        
        // 3. 调用getOneTextbookDetails接口获取教材信息
        System.out.println("    --> 调用getOneTextbookDetails接口获取教材信息...");
        long apiCallStartTime2 = System.currentTimeMillis();
        TextbookPageReturnParam bookInfo = textbookService.getOneTextbookDetails(textbookId);
        long apiCallEndTime2 = System.currentTimeMillis();
        System.out.println("    --> 教材信息获取完成, API调用耗时: " + (apiCallEndTime2 - apiCallStartTime2) + "ms");
        
        // 4. 处理教材信息，创建一个Map来精确控制输出字段
        System.out.println("    --> 处理教材信息...");
        java.util.Map<String, Object> processedBookInfo = createBookInfoWithReplacedFields(bookInfo);
        System.out.println("    --> 教材信息处理完成");
        
        // 5. 构造JS文件内容
        System.out.println("    --> 构造JS文件内容...");
        StringBuilder jsContent = new StringBuilder();
        
        // 添加章节数据
        System.out.println("    --> 序列化章节数据...");
        long jsonSerializeStart1 = System.currentTimeMillis();
        String catalogListJson = convertToJsonString(catalogList);
        long jsonSerializeEnd1 = System.currentTimeMillis();
        System.out.println("    --> 章节数据序列化完成, 耗时: " + (jsonSerializeEnd1 - jsonSerializeStart1) + "ms");
        
        jsContent.append("const catalogList = ")
                 .append(catalogListJson)
                 .append(";\n\n");
        
        // 添加教材信息
        System.out.println("    --> 序列化教材信息...");
        long jsonSerializeStart2 = System.currentTimeMillis();
        String bookInfoJson = convertToJsonString(processedBookInfo);
        long jsonSerializeEnd2 = System.currentTimeMillis();
        System.out.println("    --> 教材信息序列化完成, 耗时: " + (jsonSerializeEnd2 - jsonSerializeStart2) + "ms");
        
        jsContent.append("const bookInfo = ")
                 .append(bookInfoJson)
                 .append(";");
        
        System.out.println("    --> JS内容构造完成");

        // 6. 写入文件
        System.out.println("    --> 写入JS文件...");
        Path jsFilePath = workDir.resolve("bookData.js");
        Files.write(jsFilePath, jsContent.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("    --> JS文件写入完成: " + jsFilePath);
    }
    
    /**
     * 处理章节数据中的资源链接，确保与HTML中的资源链接处理保持一致
     * 
     * @param catalogList 章节数据列表
     * @param textbookId 教材ID
     */
    private void processResourceLinksInCatalogList(List<ReadTextbookReturnParam> catalogList, Long textbookId) {
        if (catalogList == null || catalogList.isEmpty()) {
            return;
        }
        
        // 定义教材图片的基础路径模式
        String textbookPictureBasePath = "/upload/public/picture/convertTextbookImage/" + textbookId + "/";
        System.out.println("    --> 处理资源链接，基础路径: " + textbookPictureBasePath);
        
        int processedItems = 0;
        long sectionProcessStartTime = System.currentTimeMillis();
        for (ReadTextbookReturnParam catalog : catalogList) {
            // 处理目录名称中的图片链接
            if (catalog != null && catalog.getCatalogName() != null) {
                // 使用Jsoup解析目录名称并处理图片链接
                Document doc = Jsoup.parse(catalog.getCatalogName());
                Elements images = doc.select("img");
                
                boolean hasChanged = false;
                for (Element img : images) {
                    String src = img.attr("src");
                    
                    // 检查是否包含教材图片路径
                    int index = src.indexOf(textbookPictureBasePath);
                    if (index != -1) {
                        // 从绝对路径中提取文件名
                        String fileName = new File(src.substring(index + textbookPictureBasePath.length())).getName();
                        // 更新为新的相对路径，与HTML中的处理保持一致
                        img.attr("src", "./resource/img/" + fileName);
                        hasChanged = true;
                    }
                    // 对于其他路径（如教学素材中的图片）保持原样不变
                }
                
                // 只有在有变化时才更新目录名称
                if (hasChanged) {
                    // 使用body().html()来获取处理后的HTML片段，而不是整个文档
                    catalog.setCatalogName(doc.body().html());
                }
            }
            
            // 处理内容中的图片链接
            if (catalog != null && catalog.getContent() != null) {
                // 使用Jsoup解析内容并处理图片链接
                Document doc = Jsoup.parse(catalog.getContent());
                Elements images = doc.select("img");
                
                boolean hasChanged = false;
                for (Element img : images) {
                    String src = img.attr("src");
                    
                    // 检查是否包含教材图片路径
                    int index = src.indexOf(textbookPictureBasePath);
                    if (index != -1) {
                        // 从绝对路径中提取文件名
                        String fileName = new File(src.substring(index + textbookPictureBasePath.length())).getName();
                        // 更新为新的相对路径，与HTML中的处理保持一致
                        img.attr("src", "./resource/img/" + fileName);
                        hasChanged = true;
                    }
                    // 对于其他路径（如教学素材中的图片）保持原样不变
                }
                
                // 只有在有变化时才更新内容
                if (hasChanged) {
                    // 使用body().html()来获取处理后的HTML片段，而不是整个文档
                    catalog.setContent(doc.body().html());
                }
            }
            
            processedItems++;
            if (processedItems % 100 == 0) {
                System.out.println("    --> 已处理 " + processedItems + "/" + catalogList.size() + " 个章节");
            }
            
            // 每处理500个章节输出一次详细进度
            if (processedItems % 500 == 0) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - sectionProcessStartTime;
                double avgTimePerItem = (double) elapsedTime / processedItems;
                long estimatedRemaining = (long) (avgTimePerItem * (catalogList.size() - processedItems));
                System.out.println("    --> 处理进度: " + processedItems + "/" + catalogList.size() + 
                                 " 章节, 已用时: " + elapsedTime + "ms, 预计剩余时间: " + estimatedRemaining + "ms");
            }
        }
        System.out.println("    --> 资源链接处理完成，共处理 " + catalogList.size() + " 个章节");
    }
    
    /**
     * 处理教材信息，创建一个Map，其中只包含指定的字段
     * 
     * @param bookInfo 原始教材信息
     * @return 处理后的教材信息Map，只包含教材名称、作者、简介、教材分类、创建日期五个字段
     */
    private java.util.Map<String, Object> createBookInfoWithReplacedFields(TextbookPageReturnParam bookInfo) {
        System.out.println("    --> 开始处理bookInfo，只保留指定字段");
        
        java.util.Map<String, Object> processedBookInfo = new java.util.HashMap<>();
        
        // 只添加需要的五个字段
        processedBookInfo.put("textbookName", bookInfo.getTextbookName()); // 教材名称
        
        // 作者可能在authorName或textbookAuthorName中，优先使用authorName
        String author = bookInfo.getAuthorName();
        if (author == null || author.isEmpty()) {
            author = bookInfo.getTextbookAuthorName();
        }
        processedBookInfo.put("authorName", author); // 作者
        
        processedBookInfo.put("description", bookInfo.getDescription()); // 简介
        
        // 教材分类需要通过ID获取分类名称
        String classificationName = null;
        if (bookInfo.getClassification() != null) {
            TextbookClassification classification = textbookClassificationService.getById(bookInfo.getClassification());
            if (classification != null) {
                classificationName = classification.getClassificationName();
            }
        }
        processedBookInfo.put("classification", classificationName); // 教材分类名称
        
        processedBookInfo.put("addDatetime", bookInfo.getAddDatetime()); // 创建日期
        
        System.out.println("    --> 处理完成");
        return processedBookInfo;
    }

    /**
     * 将对象转换为格式化的JSON字符串
     *
     * @param obj 要转换的对象
     * @return 格式化的JSON字符串
     */
    private String convertToJsonString(Object obj) {
        try {
            // 使用Jackson进行JSON序列化，添加适当的缩进和换行
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // 注册JavaTimeModule以支持LocalDateTime等Java 8时间类型
            
            long serializeStartTime = System.currentTimeMillis();
            String result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            long serializeEndTime = System.currentTimeMillis();
            
            // 输出序列化结果大小
            System.out.println("      ---> JSON序列化完成, 结果大小: " + result.length() + " 字符, 耗时: " + (serializeEndTime - serializeStartTime) + "ms");
            
            return result;
        } catch (Exception e) {
            // 记录异常日志，避免静默捕获异常导致问题无法追踪
            System.err.println("JSON序列化失败: " + e.getMessage());
            e.printStackTrace();
            
            // 如果序列化失败，返回空对象或数组
            if (obj instanceof List) {
                return "[]";
            } else if (obj instanceof Map) {
                return "{}";
            } else {
                return "\"\"";
            }
        }
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
        System.out.println("  -> 开始打包和编译流程...");
        
        // 1. 生成随机密码
        String password = generateRandomPassword(12);
        System.out.println(" -> 1. 生成随机密码: " + password);

        // 2. 将HTML文件和resource文件夹创建为一个加密的ZIP包
        System.out.println(" -> 2. 创建加密的内容ZIP包...");
        Path contentZipPath = workDir.resolve(outputBaseName + "_Content.zip");
        long zipStartTime = System.currentTimeMillis();
        createEncryptedZipWithResources(textbookHtmlPath, workDir.resolve("resource"), contentZipPath, password);
        long zipEndTime = System.currentTimeMillis();
        System.out.println(" -> 2. 已创建加密的内容ZIP包: " + contentZipPath + ", 耗时: " + (zipEndTime - zipStartTime) + "ms");

        // 3. 编译Go语言的解锁程序 (Windows版本)
        System.out.println(" -> 3. 编译Windows Go可执行文件...");
        Path winUnlockerPath = workDir.resolve(outputBaseName + "_Unlocker_win_amd64.exe");
        long compileStartTime = System.currentTimeMillis();
        compileGoExecutable(deviceId, password, winUnlockerPath);
        long compileEndTime = System.currentTimeMillis();
        System.out.println(" -> 3. 已编译Windows Go可执行文件: " + winUnlockerPath + ", 耗时: " + (compileEndTime - compileStartTime) + "ms");

        // 4. 编译Linux ARM64版本的解锁程序
        System.out.println(" -> 4. 编译Linux ARM64 Go可执行文件...");
        Path linuxArmUnlockerPath = workDir.resolve(outputBaseName + "_Unlocker_linux_arm64");
        long compileLinuxStartTime = System.currentTimeMillis();
        compileGoExecutableLinuxArm64(deviceId, password, linuxArmUnlockerPath);
        long compileLinuxEndTime = System.currentTimeMillis();
        System.out.println(" -> 4. 已编译Linux ARM64 Go可执行文件: " + linuxArmUnlockerPath + ", 耗时: " + (compileLinuxEndTime - compileLinuxStartTime) + "ms");

        // 5. 编译Linux AMD64版本的解锁程序
        System.out.println(" -> 5. 编译Linux AMD64 Go可执行文件...");
        Path linuxAmdUnlockerPath = workDir.resolve(outputBaseName + "_Unlocker_linux_amd64");
        long compileLinuxAmdStartTime = System.currentTimeMillis();
        compileGoExecutableLinuxAmd64(deviceId, password, linuxAmdUnlockerPath);
        long compileLinuxAmdEndTime = System.currentTimeMillis();
        System.out.println(" -> 5. 已编译Linux AMD64 Go可执行文件: " + linuxAmdUnlockerPath + ", 耗时: " + (compileLinuxAmdEndTime - compileLinuxAmdStartTime) + "ms");

        // 6. 将加密内容ZIP包和Go解锁程序打包成一个最终的ZIP包
        System.out.println(" -> 6. 创建最终下载包...");
        Path finalPackagePath = workDir.resolve(outputBaseName + "_Package.zip");
        System.out.println(" -> 6. 创建最终下载包: " + finalPackagePath);
        long packageStartTime = System.currentTimeMillis();
        try (ZipFile finalZip = new ZipFile(finalPackagePath.toFile())) {
            System.out.println("    --> 添加内容ZIP包到最终包...");
            finalZip.addFile(contentZipPath.toFile());
            System.out.println("    --> 添加Windows解锁程序到最终包...");
            finalZip.addFile(winUnlockerPath.toFile());
            System.out.println("    --> 添加Linux ARM64解锁程序到最终包...");
            finalZip.addFile(linuxArmUnlockerPath.toFile());
            System.out.println("    --> 添加Linux AMD64解锁程序到最终包...");
            finalZip.addFile(linuxAmdUnlockerPath.toFile());
        }
        long packageEndTime = System.currentTimeMillis();
        System.out.println(" -> 6. 最终下载包创建完成, 耗时: " + (packageEndTime - packageStartTime) + "ms");
        
        System.out.println("  -> 打包和编译流程完成");

        return finalPackagePath;
    }

    /**
     * 创建一个包含HTML文件和整个resource文件夹的加密ZIP。
     */
    private void createEncryptedZipWithResources(Path htmlFile, Path resourceDir, Path zipOutputPath, String password) throws IOException {
        System.out.println("    --> 开始创建加密ZIP包...");
        ZipParameters params = new ZipParameters();
        params.setEncryptFiles(true);
        params.setEncryptionMethod(EncryptionMethod.AES);
        params.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

        long zipStartTime = System.currentTimeMillis();
        try (ZipFile zipFile = new ZipFile(zipOutputPath.toFile(), password.toCharArray())) {
            // 添加HTML文件
            System.out.println("    --> 添加HTML文件: " + htmlFile);
            long addHtmlStartTime = System.currentTimeMillis();
            zipFile.addFile(htmlFile.toFile(), params);
            long addHtmlEndTime = System.currentTimeMillis();
            System.out.println("    --> HTML文件添加完成, 耗时: " + (addHtmlEndTime - addHtmlStartTime) + "ms");

            // 添加JS数据文件（与HTML文件同级）
            Path jsFile = htmlFile.getParent().resolve("bookData.js");
            if (Files.exists(jsFile)) {
                System.out.println("    --> 添加JS文件: " + jsFile);
                long addJsStartTime = System.currentTimeMillis();
                zipFile.addFile(jsFile.toFile(), params);
                long addJsEndTime = System.currentTimeMillis();
                System.out.println("    --> JS文件添加完成, 耗时: " + (addJsEndTime - addJsStartTime) + "ms");
            }
            
            // 添加resourceMap.js文件（与HTML文件同级）
            Path resourceMapFile = htmlFile.getParent().resolve("resourceMap.js");
            if (Files.exists(resourceMapFile)) {
                System.out.println("    --> 添加resourceMap.js文件: " + resourceMapFile);
                long addResourceMapStartTime = System.currentTimeMillis();
                zipFile.addFile(resourceMapFile.toFile(), params);
                long addResourceMapEndTime = System.currentTimeMillis();
                System.out.println("    --> resourceMap.js文件添加完成, 耗时: " + (addResourceMapEndTime - addResourceMapStartTime) + "ms");
            }
            
            // 添加index.html文件（与HTML文件同级）
            Path indexHtmlFile = htmlFile.getParent().resolve("index.html");
            if (Files.exists(indexHtmlFile)) {
                System.out.println("    --> 添加index.html文件: " + indexHtmlFile);
                long addIndexHtmlStartTime = System.currentTimeMillis();
                zipFile.addFile(indexHtmlFile.toFile(), params);
                long addIndexHtmlEndTime = System.currentTimeMillis();
                System.out.println("    --> index.html文件添加完成, 耗时: " + (addIndexHtmlEndTime - addIndexHtmlStartTime) + "ms");
            }

            // 如果resource文件夹存在，则添加整个文件夹
            if (Files.exists(resourceDir) && Files.isDirectory(resourceDir)) {
                System.out.println("    --> 添加资源文件夹: " + resourceDir);
                long addResourceStartTime = System.currentTimeMillis();
                zipFile.addFolder(resourceDir.toFile(), params);
                long addResourceEndTime = System.currentTimeMillis();
                System.out.println("    --> 资源文件夹添加完成, 耗时: " + (addResourceEndTime - addResourceStartTime) + "ms");
            }
        }
        long zipEndTime = System.currentTimeMillis();
        System.out.println("    --> 加密ZIP包创建完成, 总耗时: " + (zipEndTime - zipStartTime) + "ms");
    }

    /**
     * 处理教学素材，下载并生成resourceMap.js文件
     *
     * @param textbookId 教材ID
     * @param workDir    工作目录
     * @throws IOException 文件操作异常
     */
    private Map<String, Map<String, String>> processTeachingMaterials(Long textbookId, Path workDir) throws IOException {
        System.out.println("    --> 开始处理教学素材，教材ID: " + textbookId);
        System.out.println("    --> 工作目录: " + workDir.toString());
        
        // 1. 查询教材绑定的所有教学素材
        System.out.println("    --> 查询教材绑定的教学素材...");
        List<MaterialsTextbookMapping> mappings = materialsTextbookMappingService.selectMaterialsTextbookMappingByTextbookId(textbookId);
        if (mappings == null || mappings.isEmpty()) {
            System.out.println("    --> 未找到绑定的教学素材");
            // 即使没有教学素材也要生成空的resourceMap.js文件
            return new HashMap<>();
        }
        
        System.out.println("    --> 共找到 " + mappings.size() + " 个素材映射关系");
        
        // 2. 获取所有教学素材的详细信息
        List<Long> materialIds = mappings.stream()
                .map(MaterialsTextbookMapping::getMaterialId)
                .distinct()
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        System.out.println("    --> 查询教学素材详情，素材ID列表: " + materialIds);
        List<TeachingMaterials> materialsList = teachingMaterialsService.listByIds(materialIds);
        System.out.println("    --> 成功获取 " + materialsList.size() + " 个教学素材");
        
        // 3. 创建素材ID到文件信息的映射
        Map<String, Map<String, String>> resourceMap = new HashMap<>();
        
        // 4. 创建目标目录
        Path materialsDir = workDir.resolve("resource").resolve("materials");
        System.out.println("    --> 创建教学素材目录: " + materialsDir);
        Files.createDirectories(materialsDir);
        
        // 5. 处理每个教学素材
        for (TeachingMaterials material : materialsList) {
            try {
                System.out.println("    --> 开始处理教学素材: ID=" + material.getId() + ", 名称=" + material.getName() + ", 类型=" + material.getType());
                System.out.println("    --> 教学素材文件路径: " + material.getFilePath());
                
                // 只处理教学素材类型（跳过group和dataPush类型）
                if ("group".equals(material.getType()) || "dataPush".equals(material.getType())) {
                    System.out.println("    --> 跳过不需要处理的素材类型: " + material.getType());
                    continue;
                }
                
                // 根据素材类型分别处理
                if ("link".equals(material.getType())) {
                    // 处理链接类型素材
                    String url = material.getFilePath();
                    System.out.println("    --> 处理链接类型素材，URL: " + url);
                    if (url != null && !url.isEmpty()) {
                        Map<String, String> linkInfo = new HashMap<>();
                        linkInfo.put("category", "link");
                        linkInfo.put("url", url);
                        linkInfo.put("type", "link");
                        resourceMap.put(String.valueOf(material.getId()), linkInfo);
                        System.out.println("    --> 链接素材处理完成: " + url);
                    } else {
                        System.out.println("    --> 链接素材URL为空，跳过: ID=" + material.getId());
                    }
                } else {
                    // 处理普通文件类型素材
                    System.out.println("    --> 处理文件类型素材: " + material.getType());
                    String fileName = downloadMaterialFile(material, materialsDir);
                    if (fileName != null) {
                        // 下载成功，添加到资源映射中
                        Map<String, String> fileInfo = new HashMap<>();
                        fileInfo.put("category", "simple");
                        // 使用单数形式的 "resource" 路径
                        fileInfo.put("filePath", "resource/materials/" + fileName);
                        fileInfo.put("type", material.getType());
                        resourceMap.put(String.valueOf(material.getId()), fileInfo);
                        System.out.println("    --> 文件素材处理完成: " + fileName);
                    } else {
                        // 下载失败，跳过该素材（不添加到resourceMap中）
                        System.out.println("    --> 文件素材下载失败，已跳过: ID=" + material.getId());
                    }
                }
            } catch (Exception e) {
                // 容错处理：遇到任何异常都跳过该素材，继续处理下一个
                System.err.println("    --> 处理素材时发生错误，已跳过: ID=" + material.getId() + ", 错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("    --> 教学素材处理完成，共处理了 " + resourceMap.size() + " 个有效素材");
        
        // 添加调试日志，显示resourceMap中的所有键
        System.out.println("    --> 教学素材处理后 resourceMap 中的键: " + resourceMap.keySet());
        
        return resourceMap;
    }

    /**
     * 处理应用素材，将应用素材信息添加到已有的resourceMap中
     *
     * @param textbookId 教材ID
     * @param workDir    工作目录
     * @param resourceMap 已有的资源映射信息
     * @throws IOException 文件操作异常
     */
    private void processApplicationMaterials(Long textbookId, Path workDir, Map<String, Map<String, String>> resourceMap) throws IOException {
        System.out.println("    --> 开始处理应用素材，教材ID: " + textbookId);
        System.out.println("    --> 工作目录: " + workDir.toString());
        System.out.println("    --> 处理前 resourceMap 中的键: " + resourceMap.keySet());

        // 1. 查询教材绑定的所有应用素材
        System.out.println("    --> 查询教材绑定的应用素材...");
        List<ApplicationMaterials> appMaterialsList = applicationMaterialsService.listByTextbookId(textbookId);
        if (appMaterialsList == null || appMaterialsList.isEmpty()) {
            System.out.println("    --> 未找到绑定的应用素材");
            return;
        }

        System.out.println("    --> 共找到 " + appMaterialsList.size() + " 个应用素材");

        // 2. 创建应用素材根目录
        Path groupsDir = workDir.resolve("resource").resolve("groups");
        System.out.println("    --> 创建应用素材根目录: " + groupsDir);
        Files.createDirectories(groupsDir);

        // 3. 处理每个应用素材
        for (ApplicationMaterials appMaterial : appMaterialsList) {
            try {
                System.out.println("    --> 开始处理应用素材: ID=" + appMaterial.getId() + ", 名称=" + appMaterial.getName());

                // 为每个应用素材创建独立的文件夹
                Path appDir = groupsDir.resolve(String.valueOf(appMaterial.getId()));
                System.out.println("    --> 创建应用素材独立目录: " + appDir);
                Files.createDirectories(appDir);

                // 获取应用素材详细信息，包括关联的教学素材
                ApplicationMaterialsVO appMaterialDetail = applicationMaterialsService.getApplicationMaterialsById(
                        appMaterial.getId(), true);

                if (appMaterialDetail.getTeachingMaterials() == null || 
                    appMaterialDetail.getTeachingMaterials().isEmpty()) {
                    System.out.println("    --> 应用素材没有关联的教学素材，跳过: ID=" + appMaterial.getId());
                    continue;
                }

                // 为应用素材创建复合资源结构
                Map<String, String> appResourceInfo = new HashMap<>();
                appResourceInfo.put("category", "complex");
                appResourceInfo.put("type", "group");
                appResourceInfo.put("title", appMaterial.getName());
                
                // 创建元数据
                Map<String, String> metaInfo = new HashMap<>();
                // 获取创建者信息
                String creatorName = "未知创建者";
                if (appMaterial.getCreator() != null) {
                    // 通过教师服务获取创建者名称
                    try {
                        Teacher teacher = teacherMapper.selectById(appMaterial.getCreator());
                        if (teacher != null && teacher.getName() != null) {
                            creatorName = teacher.getName();
                        } else {
                            creatorName = "教师ID:" + appMaterial.getCreator();
                        }
                    } catch (Exception e) {
                        System.err.println("    --> 获取教师信息失败: " + e.getMessage());
                        creatorName = "教师ID:" + appMaterial.getCreator();
                    }
                }
                metaInfo.put("creator", creatorName);
                
                // 获取创建日期
                String createDate = "";
                if (appMaterial.getAddDatetime() != null) {
                    createDate = appMaterial.getAddDatetime().toString();
                }
                metaInfo.put("date", createDate);
                
                // 获取教材名称
                String textbookName = "未知教材";
                Textbook textbook = textbookMapper.selectById(textbookId);
                if (textbook != null) {
                    textbookName = textbook.getTextbookName();
                }
                metaInfo.put("textbook", textbookName);
                
                // 获取描述信息
                String description = appMaterial.getDescription() != null ? appMaterial.getDescription() : "";
                metaInfo.put("desc", description);
                
                // 将元数据转换为字符串存储
                appResourceInfo.put("meta_json", convertToJsonString(metaInfo));
                
                // 创建子文件列表
                List<Map<String, String>> children = new ArrayList<>();
                
                // 处理每个关联的教学素材
                for (ApplicationMaterialsDetailVO detailVO : appMaterialDetail.getTeachingMaterials()) {
                    System.out.println("    --> 处理关联的教学素材: ID=" + detailVO.getTeachingMaterialId() + 
                                     ", 名称=" + detailVO.getTeachingMaterialName() + ", 类型=" + detailVO.getType());
                    
                    // 检查这个教学素材是否已经在resourceMap中存在
                    if (resourceMap.containsKey(String.valueOf(detailVO.getTeachingMaterialId()))) {
                        System.out.println("    --> 教学素材已在resourceMap中存在: ID=" + detailVO.getTeachingMaterialId());
                    }
                    
                    if ("link".equals(detailVO.getType())) {
                        // 处理链接类型素材
                        String url = detailVO.getFilePath();
                        System.out.println("    --> 处理链接类型素材，URL: " + url);
                        if (url != null && !url.isEmpty()) {
                            // 添加到应用素材的子文件列表
                            Map<String, String> childInfo = new HashMap<>();
                            childInfo.put("name", detailVO.getTeachingMaterialName() != null ? detailVO.getTeachingMaterialName() : "未知链接");
                            childInfo.put("type", "link");
                            childInfo.put("path", url);
                            children.add(childInfo);
                            
                            System.out.println("    --> 链接素材处理完成: " + url);
                        } else {
                            System.out.println("    --> 链接素材URL为空，跳过: ID=" + detailVO.getTeachingMaterialId());
                        }
                    } else {
                        // 处理普通文件类型素材
                        System.out.println("    --> 处理文件类型素材: " + detailVO.getType());
                        
                        // 获取教学素材实体以获取文件名信息
                        TeachingMaterials teachingMaterial = teachingMaterialsService.getById(detailVO.getTeachingMaterialId());
                        if (teachingMaterial == null) {
                            System.out.println("    --> 未找到教学素材: ID=" + detailVO.getTeachingMaterialId());
                            continue;
                        }
                        
                        // 获取原始文件名
                        String originalFileName = teachingMaterial.getFileName();
                        if (originalFileName == null || originalFileName.isEmpty()) {
                            // 如果没有存储原始文件名，则从文件路径中提取
                            Path sourceFile = getMaterialFilePath(teachingMaterial, null);
                            originalFileName = sourceFile.getFileName().toString();
                            System.out.println("    --> 从文件路径提取文件名: " + originalFileName);
                        }
                        
                        // 对文件名进行清洗，将空格、括号等特殊符号替换为下划线
                        String sanitizedFileName = originalFileName.replaceAll("[\\s()\\[\\]{}<>:\"|?*\\\\/]", "_");
                        System.out.println("    --> 清洗后的文件名: " + sanitizedFileName);
                        
                        // 分离文件名和扩展名
                        String fileNameWithoutExtension = sanitizedFileName;
                        String extension = "";
                        int lastDotIndex = sanitizedFileName.lastIndexOf('.');
                        if (lastDotIndex > 0) {
                            fileNameWithoutExtension = sanitizedFileName.substring(0, lastDotIndex);
                            extension = sanitizedFileName.substring(lastDotIndex); // 包含点号
                        }
                        
                        // 构造目标文件名：{teachingMaterialId}_{原始文件名}.{后缀}
                        String targetFileName = detailVO.getTeachingMaterialId() + "_" + fileNameWithoutExtension + extension;
                        Path targetFile = appDir.resolve(targetFileName);
                        System.out.println("    --> 目标文件路径: " + targetFile.toString());
                        
                        // 检查是否已存在以该ID开头的文件，如果存在则跳过下载
                        boolean fileExists = false;
                        try (Stream<Path> files = Files.list(appDir)) {
                            fileExists = files.anyMatch(f -> f.getFileName().toString().startsWith(detailVO.getTeachingMaterialId() + "_"));
                        }
                        System.out.println("    --> 检查文件是否已存在: " + fileExists);
                        
                        if (!fileExists) {
                            // 下载文件
                            System.out.println("    --> 开始下载教学素材文件: ID=" + detailVO.getTeachingMaterialId());
                            try {
                                Path sourceFilePath = getMaterialFilePath(teachingMaterial, null);
                                System.out.println("    --> 源文件路径: " + sourceFilePath.toString());
                                
                                if (Files.exists(sourceFilePath)) {
                                    Files.copy(sourceFilePath, targetFile, StandardCopyOption.REPLACE_EXISTING);
                                    System.out.println("    --> 文件复制完成: " + targetFile);
                                } else {
                                    System.out.println("    --> 源文件不存在: " + sourceFilePath);
                                    continue;
                                }
                            } catch (Exception e) {
                                System.err.println("    --> 文件下载失败: ID=" + detailVO.getTeachingMaterialId() + ", 错误: " + e.getMessage());
                                e.printStackTrace();
                                continue;
                            }
                        }
                        
                        // 添加到应用素材的子文件列表
                        Map<String, String> childInfo = new HashMap<>();
                        childInfo.put("name", originalFileName);
                        childInfo.put("type", detailVO.getType());
                        childInfo.put("path", "resource/groups/" + appMaterial.getId() + "/" + targetFileName);
                        children.add(childInfo);
                        
                        System.out.println("    --> 文件素材处理完成: " + targetFileName);
                    }
                }
                
                // 将子文件列表转换为字符串存储
                appResourceInfo.put("children_json", convertToJsonString(children));
                
                // 将应用素材信息添加到资源映射中
                resourceMap.put(String.valueOf(appMaterial.getId()), appResourceInfo);
                
                System.out.println("    --> 应用素材处理完成: ID=" + appMaterial.getId());
            } catch (Exception e) {
                // 容错处理：遇到任何异常都跳过该素材，继续处理下一个
                System.err.println("    --> 处理应用素材时发生错误，已跳过: ID=" + appMaterial.getId() + ", 错误: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("    --> 应用素材处理完成");
        System.out.println("    --> 处理后 resourceMap 中的键: " + resourceMap.keySet());
    }

    /**
     * 处理资料推送，将资料推送信息添加到已有的resourceMap中
     *
     * @param textbookId 教材ID
     * @param workDir    工作目录
     * @param resourceMap 已有的资源映射信息
     * @throws IOException 文件操作异常
     */
    private void processMaterialPushes(Long textbookId, Path workDir, Map<String, Map<String, String>> resourceMap) throws IOException {
        System.out.println("    --> 开始处理资料推送，教材ID: " + textbookId);
        System.out.println("    --> 工作目录: " + workDir.toString());
        System.out.println("    --> 处理前 resourceMap 中的键: " + resourceMap.keySet());

        // 1. 查询教材绑定的所有资料推送
        System.out.println("    --> 查询教材绑定的资料推送...");
        List<MaterialPush> materialPushList = materialPushService.listByTextbookId(textbookId);
        if (materialPushList == null || materialPushList.isEmpty()) {
            System.out.println("    --> 未找到绑定的资料推送");
            return;
        }

        System.out.println("    --> 共找到 " + materialPushList.size() + " 个资料推送");

        // 2. 创建资料推送根目录
        Path pushesDir = workDir.resolve("resource").resolve("pushes");
        System.out.println("    --> 创建资料推送根目录: " + pushesDir);
        Files.createDirectories(pushesDir);

        // 3. 处理每个资料推送
        for (MaterialPush materialPush : materialPushList) {
            try {
                System.out.println("    --> 开始处理资料推送: ID=" + materialPush.getId() + ", 名称=" + materialPush.getName());

                // 为每个资料推送创建独立的文件夹
                Path pushDir = pushesDir.resolve(String.valueOf(materialPush.getId()));
                System.out.println("    --> 创建资料推送独立目录: " + pushDir);
                Files.createDirectories(pushDir);

                // 获取资料推送详细信息，包括关联的附件列表
                List<MaterialList> materialList = materialPushService.listMaterialListByTextbookId(textbookId).stream()
                        .filter(ml -> ml.getMaterialPushId().equals(materialPush.getId()))
                        .collect(Collectors.toList());

                if (materialList == null || materialList.isEmpty()) {
                    System.out.println("    --> 资料推送没有关联的附件，跳过: ID=" + materialPush.getId());
                    continue;
                }

                // 为资料推送创建复合资源结构
                Map<String, String> pushResourceInfo = new HashMap<>();
                pushResourceInfo.put("category", "complex");
                pushResourceInfo.put("type", "dataPush");
                pushResourceInfo.put("title", materialPush.getName());

                // 创建元数据
                Map<String, String> metaInfo = new HashMap<>();
                
                // 获取关联章节信息
                String chapterName = "未知章节";
                if (materialPush.getTextbookCatalogId() != null) {
                    TextbookCatalog catalog = textbookCatalogService.getById(materialPush.getTextbookCatalogId());
                    if (catalog != null && catalog.getCatalogName() != null) {
                        // 去除HTML标签
                        chapterName = Jsoup.parse(catalog.getCatalogName()).text();
                    }
                }
                metaInfo.put("chapter", chapterName);
                
                // 获取资料推送类型
                String pushType = materialPush.getType() != null ? materialPush.getType() : "未知类型";
                metaInfo.put("pushType", pushType);
                
                // 获取资料推送简介
                String intro = materialPush.getIntroduction() != null ? materialPush.getIntroduction() : "";
                metaInfo.put("intro", intro);
                
                // 将元数据转换为字符串存储
                pushResourceInfo.put("meta_json", convertToJsonString(metaInfo));

                // 创建子文件列表
                List<Map<String, String>> children = new ArrayList<>();

                // 处理每个关联的附件
                for (MaterialList materialListItem : materialList) {
                    System.out.println("    --> 处理关联的附件: ID=" + materialListItem.getId() +
                            ", 名称=" + materialListItem.getName() + ", 类型=" + materialListItem.getType());

                    if ("link".equals(materialListItem.getType())) {
                        // 处理链接类型附件
                        String url = materialListItem.getAddress();
                        System.out.println("    --> 处理链接类型附件，URL: " + url);
                        if (url != null && !url.isEmpty()) {
                            // 添加到资料推送的子文件列表
                            Map<String, String> childInfo = new HashMap<>();
                            childInfo.put("name", materialListItem.getName() != null ? materialListItem.getName() : "未知链接");
                            childInfo.put("type", "link");
                            childInfo.put("path", url);
                            children.add(childInfo);

                            System.out.println("    --> 链接附件处理完成: " + url);
                        } else {
                            System.out.println("    --> 链接附件URL为空，跳过: ID=" + materialListItem.getId());
                        }
                    } else {
                        // 处理普通文件类型附件
                        System.out.println("    --> 处理文件类型附件: " + materialListItem.getType());

                        // 获取原始文件名
                        String originalFileName = materialListItem.getName();
                        if (originalFileName == null || originalFileName.isEmpty()) {
                            System.out.println("    --> 附件名称为空，跳过: ID=" + materialListItem.getId());
                            continue;
                        }

                        // 对文件名进行清洗，将空格、括号等特殊符号替换为下划线
                        String sanitizedFileName = originalFileName.replaceAll("[\\s()\\[\\]{}<>:\"|?*\\\\/]", "_");
                        System.out.println("    --> 清洗后的文件名: " + sanitizedFileName);

                        // 分离文件名和扩展名
                        String fileNameWithoutExtension = sanitizedFileName;
                        String extension = "";
                        int lastDotIndex = sanitizedFileName.lastIndexOf('.');
                        if (lastDotIndex > 0) {
                            fileNameWithoutExtension = sanitizedFileName.substring(0, lastDotIndex);
                            extension = sanitizedFileName.substring(lastDotIndex); // 包含点号
                        }

                        // 构造目标文件名：{materialListId}_{原始文件名}.{后缀}
                        String targetFileName = materialListItem.getId() + "_" + fileNameWithoutExtension + extension;
                        Path targetFile = pushDir.resolve(targetFileName);
                        System.out.println("    --> 目标文件路径: " + targetFile.toString());

                        // 检查是否已存在以该ID开头的文件，如果存在则跳过下载
                        boolean fileExists = false;
                        try (Stream<Path> files = Files.list(pushDir)) {
                            fileExists = files.anyMatch(f -> f.getFileName().toString().startsWith(materialListItem.getId() + "_"));
                        }
                        System.out.println("    --> 检查文件是否已存在: " + fileExists);

                        if (!fileExists) {
                            // 下载文件
                            System.out.println("    --> 开始下载附件文件: ID=" + materialListItem.getId());
                            try {
                                Path sourceFilePath = getMaterialPushFilePath(materialListItem);
                                System.out.println("    --> 源文件路径: " + sourceFilePath.toString());

                                if (Files.exists(sourceFilePath)) {
                                    Files.copy(sourceFilePath, targetFile, StandardCopyOption.REPLACE_EXISTING);
                                    System.out.println("    --> 文件复制完成: " + targetFile);
                                } else {
                                    System.out.println("    --> 源文件不存在: " + sourceFilePath);
                                    continue;
                                }
                            } catch (Exception e) {
                                System.err.println("    --> 文件下载失败: ID=" + materialListItem.getId() + ", 错误: " + e.getMessage());
                                e.printStackTrace();
                                // 即使文件下载失败，我们也继续处理，不中断整个流程
                                continue;
                            }
                        }

                        // 添加到资料推送的子文件列表
                        Map<String, String> childInfo = new HashMap<>();
                        childInfo.put("name", originalFileName);
                        childInfo.put("type", materialListItem.getType());
                        childInfo.put("path", "resource/pushes/" + materialPush.getId() + "/" + targetFileName);
                        children.add(childInfo);

                        System.out.println("    --> 文件附件处理完成: " + targetFileName);
                    }
                }

                // 将子文件列表转换为字符串存储
                pushResourceInfo.put("children_json", convertToJsonString(children));

                // 将资料推送信息添加到资源映射中
                resourceMap.put(String.valueOf(materialPush.getId()), pushResourceInfo);

                System.out.println("    --> 资料推送处理完成: ID=" + materialPush.getId());
            } catch (Exception e) {
                // 容错处理：遇到任何异常都跳过该推送，继续处理下一个
                System.err.println("    --> 处理资料推送时发生错误，已跳过: ID=" + materialPush.getId() + ", 错误: " + e.getMessage());
                e.printStackTrace();
                // 继续处理下一个推送，不中断整个流程
            }
        }

        System.out.println("    --> 资料推送处理完成");
        System.out.println("    --> 处理后 resourceMap 中的键: " + resourceMap.keySet());
    }

    /**
     * 获取资料推送文件路径
     * 
     * @param materialListItem 资料推送附件
     * @return 文件路径
     * @throws IOException IO异常
     */
    private Path getMaterialPushFilePath(MaterialList materialListItem) throws IOException {
        System.out.println("    --> getMaterialPushFilePath 开始处理附件: ID=" + materialListItem.getId() + ", 类型=" + materialListItem.getType());
        System.out.println("    --> getMaterialPushFilePath 数据库中的文件路径: " + materialListItem.getAddress());

        // 检查filePath是否为绝对路径，如果不是则加上基础路径
        Path rawPath = Paths.get(materialListItem.getAddress());
        System.out.println("    --> getMaterialPushFilePath 原始路径是否为绝对路径: " + rawPath.isAbsolute());
        System.out.println("    --> getMaterialPushFilePath 配置的基础路径: " + TEACHING_MATERIALS_BASE_PATH);

        Path filePath;
        if (rawPath.isAbsolute()) {
            filePath = rawPath;
        } else {
            // 使用配置的基础路径
            filePath = Paths.get(TEACHING_MATERIALS_BASE_PATH, materialListItem.getAddress());
        }

        System.out.println("    --> getMaterialPushFilePath 拼接后的路径: " + filePath.toString());
        System.out.println("    --> getMaterialPushFilePath 文件是否存在: " + Files.exists(filePath));
        
        if (!Files.exists(filePath)) {
            System.out.println("    --> getMaterialPushFilePath 文件不存在，将抛出异常");
            throw new IOException("文件不存在");
        }

        return filePath;
    }

    /**
     * 生成resourceMap.js文件
     *
     * @param resourceMap 资源映射信息
     * @param workDir 工作目录
     * @throws IOException 文件操作异常
     */
    private void generateResourceMapJs(Map<String, Map<String, String>> resourceMap, Path workDir) throws IOException {
        System.out.println("    --> 开始生成resourceMap.js文件...");
        System.out.println("    --> resourceMap 中的键数量: " + resourceMap.size());
        System.out.println("    --> resourceMap 中的键列表: " + resourceMap.keySet());
        
        // 构造JS文件内容
        StringBuilder jsContent = new StringBuilder();
        jsContent.append("// 教学素材资源映射文件\n");
        jsContent.append("// 由系统自动生成，请勿手动修改\n\n");
        
        // 转换为JSON格式
        jsContent.append("const resourceMap = ");
        
        // 手动构建JSON对象，特殊处理包含 _json 后缀的字段
        jsContent.append("{\n");
        boolean firstEntry = true;
        for (Map.Entry<String, Map<String, String>> entry : resourceMap.entrySet()) {
            if (!firstEntry) {
                jsContent.append(",\n");
            }
            firstEntry = false;
            
            jsContent.append("  \"").append(entry.getKey()).append("\" : {\n");
            
            Map<String, String> valueMap = entry.getValue();
            boolean firstField = true;
            for (Map.Entry<String, String> fieldEntry : valueMap.entrySet()) {
                if (!firstField) {
                    jsContent.append(",\n");
                }
                firstField = false;
                
                String fieldName = fieldEntry.getKey();
                String fieldValue = fieldEntry.getValue();
                
                jsContent.append("    \"").append(fieldName).append("\" : ");
                
                // 特殊处理JSON字段
                if (fieldName.endsWith("_json")) {
                    // 移除 _json 后缀作为实际字段名
                    String actualFieldName = fieldName.substring(0, fieldName.length() - 5);
                    // 格式化JSON内容，添加适当的缩进
                    String formattedJson = formatJson(fieldValue, 4); // 4层缩进
                    jsContent.append(formattedJson);
                } else {
                    // 普通字段，添加引号
                    jsContent.append("\"").append(fieldValue != null ? fieldValue.replace("\\", "\\\\").replace("\"", "\\\"") : "").append("\"");
                }
            }
            
            jsContent.append("\n  }");
        }
        jsContent.append("\n};\n");
        
        // 移除模块化导出语法，只保留 const resourceMap = { ... };
        
        // 写入文件到工作目录根目录下，与其他JS文件保持一致
        Path jsFilePath = workDir.resolve("resourceMap.js");
        Files.write(jsFilePath, jsContent.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("    --> resourceMap.js文件生成完成: " + jsFilePath);
        
        // 添加调试信息，输出生成的文件内容的前1000个字符
        String contentPreview = jsContent.toString();
        if (contentPreview.length() > 1000) {
            contentPreview = contentPreview.substring(0, 1000) + "...(内容过长，省略后续部分)";
        }
        System.out.println("    --> resourceMap.js 文件内容预览:\n" + contentPreview);
    }
    
    /**
     * 格式化JSON字符串，添加适当的缩进
     * 
     * @param jsonStr 原始JSON字符串
     * @param indentLevel 缩进层级
     * @return 格式化后的JSON字符串
     */
    private String formatJson(String jsonStr, int indentLevel) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return "\"\"";
        }
        
        try {
            // 去掉首尾的引号（如果有的话）
            String cleanJson = jsonStr.trim();
            if (cleanJson.startsWith("\"") && cleanJson.endsWith("\"")) {
                cleanJson = cleanJson.substring(1, cleanJson.length() - 1);
                // 处理转义字符
                cleanJson = cleanJson.replace("\\\"", "\"").replace("\\\\", "\\");
            }
            
            // 解析JSON以确保它是有效的
            ObjectMapper mapper = new ObjectMapper();
            Object jsonObject = mapper.readValue(cleanJson, Object.class);
            
            // 重新序列化为格式化的JSON
            String formattedJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            
            // 添加适当的缩进
            String[] lines = formattedJson.split("\n");
            StringBuilder indentedJson = new StringBuilder();
            String indent = "";
            for (int i = 0; i < indentLevel; i++) {
                indent += "    ";
            }
            for (String line : lines) {
                indentedJson.append(indent).append(line).append("\n");
            }
            
            return indentedJson.toString();
        } catch (Exception e) {
            // 记录异常日志，避免静默捕获异常导致问题无法追踪
            System.err.println("JSON格式化失败: " + e.getMessage());
            e.printStackTrace();
            
            // 如果格式化失败，至少去掉首尾引号
            String result = jsonStr.trim();
            if (result.startsWith("\"") && result.endsWith("\"")) {
                result = result.substring(1, result.length() - 1);
            }
            return result;
        }
    }

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

    /**
     * 编译Linux ARM64版本的Go可执行文件
     */
    private void compileGoExecutableLinuxArm64(String deviceId, String password, Path exeOutputPath) throws IOException, InterruptedException {
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
                    "-o", exeOutputPath.toString(),
                    tempGoFile.getFileName().toString()
            );

            pb.environment().put("GOOS", "linux");
            pb.environment().put("GOARCH", "arm64");
            pb.environment().put("CGO_ENABLED", "0");
            pb.directory(workspaceDir.toFile());

            System.out.println(" ... 开始编译Linux ARM64 Go程序，工作目录: " + workspaceDir + " ...");
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

    /**
     * 编译Linux AMD64版本的Go可执行文件
     */
    private void compileGoExecutableLinuxAmd64(String deviceId, String password, Path exeOutputPath) throws IOException, InterruptedException {
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
                    "-o", exeOutputPath.toString(),
                    tempGoFile.getFileName().toString()
            );

            pb.environment().put("GOOS", "linux");
            pb.environment().put("GOARCH", "amd64");
            pb.environment().put("CGO_ENABLED", "0");
            pb.directory(workspaceDir.toFile());

            System.out.println(" ... 开始编译Linux AMD64 Go程序，工作目录: " + workspaceDir + " ...");
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

    /**
     * 下载单个教学素材文件（仅适用于需要下载的文件类型）
     *
     * @param material 教学素材实体
     * @param targetDir 目标目录
     * @return 下载后的文件名，如果失败则返回null
     * @throws IOException 文件操作异常
     */
    private String downloadMaterialFile(TeachingMaterials material, Path targetDir) throws IOException {
        System.out.println("    --> 开始下载教学素材文件: ID=" + material.getId() + ", 名称=" + material.getName() + ", 类型=" + material.getType());
        System.out.println("    --> 教学素材文件路径: " + material.getFilePath());
        System.out.println("    --> 教学素材文件名: " + material.getFileName());
        
        // 构造目标文件名：{id}_{原始文件名}.{后缀}
        String originalFileName = material.getFileName();
        if (originalFileName == null || originalFileName.isEmpty()) {
            // 如果没有存储原始文件名，则从文件路径中提取
            Path sourceFile = getMaterialFilePath(material, null);
            originalFileName = sourceFile.getFileName().toString();
            System.out.println("    --> 从文件路径提取文件名: " + originalFileName);
        }
        
        // 对文件名进行清洗，将空格、括号等特殊符号替换为下划线
        String sanitizedFileName = originalFileName.replaceAll("[\\s()\\[\\]{}<>:\"|?*\\\\/]", "_");
        System.out.println("    --> 清洗后的文件名: " + sanitizedFileName);
        
        String extension = "";
        int lastDotIndex = sanitizedFileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = sanitizedFileName.substring(lastDotIndex); // 包含点号
            sanitizedFileName = sanitizedFileName.substring(0, lastDotIndex);
        }
        
        String targetFileName = material.getId() + "_" + sanitizedFileName + extension;
        Path targetFile = targetDir.resolve(targetFileName);
        System.out.println("    --> 目标文件路径: " + targetFile.toString());
        
        // 检查是否已存在以该ID开头的文件，如果存在则跳过下载
        boolean fileExists = false;
        try (Stream<Path> files = Files.list(targetDir)) {
            fileExists = files.anyMatch(f -> f.getFileName().toString().startsWith(material.getId() + "_"));
        }
        System.out.println("    --> 检查文件是否已存在: " + fileExists);
        
        if (fileExists) {
            System.out.println("    --> 文件已存在，跳过下载: " + targetFileName);
            return targetFileName;
        }
        
        // 直接读取文件并写入目标文件，避免通过服务层接口
        System.out.println("    --> 直接读取素材文件并写入目标文件: ID=" + material.getId());
        try {
            Path sourceFilePath = getMaterialFilePath(material, null);
            System.out.println("    --> 源文件路径: " + sourceFilePath.toString());
            
            if (Files.exists(sourceFilePath)) {
                Files.copy(sourceFilePath, targetFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("    --> 文件复制完成: " + targetFile);
                return targetFileName;
            } else {
                System.out.println("    --> 源文件不存在: " + sourceFilePath);
                return null;
            }
        } catch (Exception e) {
            // 容错处理：遇到任何异常都返回null，表示下载失败
            System.err.println("    --> 文件下载失败: ID=" + material.getId() + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取教学素材文件路径（复用TeachingMaterialsServiceImpl中的逻辑）
     * 
     * @param materials 教学素材实体
     * @param imageSetId 图集ID（对于非图集素材传null）
     * @return 文件路径
     * @throws IOException IO异常
     */
    private Path getMaterialFilePath(TeachingMaterials materials, Integer imageSetId) throws IOException {
        System.out.println("    --> getMaterialFilePath 开始处理素材: ID=" + materials.getId() + ", 类型=" + materials.getType());
        System.out.println("    --> getMaterialFilePath 数据库中的文件路径: " + materials.getFilePath());
        
        Path filePath;
        if (materials.getType().equals("imageSet")) {
            System.out.println("    --> getMaterialFilePath 处理imageSet类型素材");
            Path dir = Paths.get(materials.getFilePath());
            List<Path> files;
            try (Stream<Path> stream = Files.walk(dir)) {
                files = stream.filter(Files::isRegularFile).collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("获取图片失败", e);
            }
            if (imageSetId != null && (imageSetId < 0 || imageSetId >= files.size()))
                throw new IllegalArgumentException("图片不存在");
            filePath = imageSetId != null ? files.get(imageSetId) : dir;
        } else {
            System.out.println("    --> getMaterialFilePath 处理普通类型素材");
            // 检查filePath是否为绝对路径，如果不是则加上基础路径
            Path rawPath = Paths.get(materials.getFilePath());
            System.out.println("    --> getMaterialFilePath 原始路径是否为绝对路径: " + rawPath.isAbsolute());
            System.out.println("    --> getMaterialFilePath 配置的基础路径: " + TEACHING_MATERIALS_BASE_PATH);
            
            if (rawPath.isAbsolute()) {
                filePath = rawPath;
            } else {
                // 使用配置的基础路径
                // 注意：数据库中存储的路径可能是相对于textbook-app的路径，所以基础路径只需要是textbook-app目录
                filePath = Paths.get(TEACHING_MATERIALS_BASE_PATH, materials.getFilePath());
            }
            
            System.out.println("    --> getMaterialFilePath 拼接后的路径: " + filePath.toString());

            // 对于 H5 和 simulation 类型，如果 filePath 指向的是目录，则尝试查找同级的原始压缩包
            // 如果路径不存在且是目录，则直接在该路径的同级目录下查找压缩包文件
            if (("H5".equalsIgnoreCase(materials.getType()) || "simulation".equalsIgnoreCase(materials.getType()))) {
                System.out.println("    --> getMaterialFilePath 处理H5或simulation类型素材");
                
                // 允许的压缩包后缀
                String[] exts = {".zip", ".7z", ".tar", ".tgz"};
                Path candidate = null;

                if (Files.isDirectory(filePath)) {
                    // 如果路径是一个已存在的目录，则在同级目录下查找原始压缩包
                    System.out.println("    --> getMaterialFilePath 路径是目录，查找同级压缩包");
                    Path parent = filePath.getParent();
                    String baseName = filePath.getFileName().toString();

                    // 首先尝试精确匹配 baseName + ext
                    for (String ext : exts) {
                        Path p1 = parent.resolve(baseName + ext);
                        System.out.println("    --> getMaterialFilePath 检查文件是否存在: " + p1.toString() + ", 存在: " + (Files.exists(p1) && Files.isRegularFile(p1)));
                        if (Files.exists(p1) && Files.isRegularFile(p1)) {
                            candidate = p1;
                            break;
                        }
                    }

                    // 如果上面没找到，再退一步：在 parent 目录下遍历，
                    // 找「文件名以 baseName 开头且后缀在 exts 内」的文件
                    if (candidate == null) {
                        try (java.nio.file.DirectoryStream<Path> stream = Files.newDirectoryStream(parent)) {
                            for (Path p : stream) {
                                if (Files.isRegularFile(p)) {
                                    String name = p.getFileName().toString();
                                    if (name.startsWith(baseName)) {
                                        for (String ext : exts) {
                                            if (name.endsWith(ext)) {
                                            candidate = p;
                                            System.out.println("    --> getMaterialFilePath 找到候选文件: " + candidate.toString());
                                            break;
                                            }
                                        }
                                    }
                                }
                                if (candidate != null) {
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Failed to search directory for original archive: " + parent);
                        }
                    }
                } else if (!Files.exists(filePath)) {
                    // 如果路径不存在且应该是一个目录，则直接在同级目录下查找压缩包
                    System.out.println("    --> getMaterialFilePath 路径不存在，直接查找同级压缩包");
                    Path parent = filePath.getParent();
                    String baseName = filePath.getFileName().toString();

                    // 首先尝试精确匹配 baseName + ext
                    for (String ext : exts) {
                        Path p1 = parent.resolve(baseName + ext);
                        System.out.println("    --> getMaterialFilePath 检查文件是否存在: " + p1.toString() + ", 存在: " + (Files.exists(p1) && Files.isRegularFile(p1)));
                        if (Files.exists(p1) && Files.isRegularFile(p1)) {
                            candidate = p1;
                            break;
                        }
                    }

                    // 如果上面没找到，再退一步：在 parent 目录下遍历，
                    // 找「文件名以 baseName 开头且后缀在 exts 内」的文件
                    if (candidate == null) {
                        try (java.nio.file.DirectoryStream<Path> stream = Files.newDirectoryStream(parent)) {
                            for (Path p : stream) {
                                if (Files.isRegularFile(p)) {
                                    String name = p.getFileName().toString();
                                    if (name.startsWith(baseName)) {
                                        for (String ext : exts) {
                                            if (name.endsWith(ext)) {
                                                candidate = p;
                                                System.out.println("    --> getMaterialFilePath 找到候选文件: " + candidate.toString());
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (candidate != null) {
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Failed to search directory for original archive: " + parent);
                        }
                    }
                }

                // 如果找到压缩包，就用 candidate 作为真正的下载文件
                if (candidate != null) {
                    filePath = candidate;
                    System.out.println("    --> getMaterialFilePath 使用候选文件: " + filePath.toString());
                } else {
                    System.out.println("    --> getMaterialFilePath 未找到原始压缩包文件");
                }
            }
        }

        System.out.println("    --> getMaterialFilePath 最终文件路径: " + filePath.toString() + ", 文件是否存在: " + Files.exists(filePath));
        if (!Files.exists(filePath))
            throw new IOException("文件不存在");

        return filePath;
    }

    /**
     * HttpServletResponse模拟类，用于捕获文件下载数据
     */
    private static class HttpServletResponseMock implements javax.servlet.http.HttpServletResponse {
        private final ByteArrayOutputStream buffer;
        private String contentType;
        private int contentLength;
        private final Map<String, String> headers = new HashMap<>();
        private int bufferSize = 8192;
        
        public HttpServletResponseMock(ByteArrayOutputStream buffer) {
            this.buffer = buffer;
        }
        
        @Override
        public void setContentLength(int len) {
            this.contentLength = len;
        }
        
        @Override
        public void setContentType(String type) {
            this.contentType = type;
        }
        
        @Override
        public javax.servlet.ServletOutputStream getOutputStream() throws IOException {
            return new javax.servlet.ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(javax.servlet.WriteListener writeListener) {
                    // 空实现
                }

                @Override
                public void write(int b) throws IOException {
                    buffer.write(b);
                }
                
                @Override
                public void write(byte[] b) throws IOException {
                    buffer.write(b);
                }
                
                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    buffer.write(b, off, len);
                }
            };
        }
        
        public byte[] getBufferData() {
            return buffer.toByteArray();
        }
        
        public int getBufferContentSize() {
            return buffer.size();
        }
        
        @Override
        public void reset() {
            // 重置操作
        }
        
        // 实现其他必需的方法
        @Override
        public void addCookie(javax.servlet.http.Cookie cookie) {}
        
        @Override
        public boolean containsHeader(String name) {
            return headers.containsKey(name);
        }
        
        @Override
        public String encodeURL(String url) {
            return url;
        }
        
        @Override
        public String encodeRedirectURL(String url) {
            return url;
        }
        
        @Override
        public String encodeUrl(String url) {
            return url;
        }
        
        @Override
        public String encodeRedirectUrl(String url) {
            return url;
        }
        
        @Override
        public void sendError(int sc, String msg) throws IOException {}
        
        @Override
        public void sendError(int sc) throws IOException {}
        
        @Override
        public void sendRedirect(String location) throws IOException {}
        
        @Override
        public void setDateHeader(String name, long date) {
            headers.put(name, String.valueOf(date));
        }
        
        @Override
        public void addDateHeader(String name, long date) {
            headers.put(name, String.valueOf(date));
        }
        
        @Override
        public void setHeader(String name, String value) {
            headers.put(name, value);
        }
        
        @Override
        public void addHeader(String name, String value) {
            headers.put(name, value);
        }
        
        @Override
        public void setIntHeader(String name, int value) {
            headers.put(name, String.valueOf(value));
        }
        
        @Override
        public void addIntHeader(String name, int value) {
            headers.put(name, String.valueOf(value));
        }
        
        @Override
        public void setStatus(int sc) {}
        
        @Override
        public void setStatus(int sc, String sm) {}
        
        @Override
        public int getStatus() {
            return 200; // 默认状态码
        }
        
        @Override
        public String getHeader(String name) {
            return headers.get(name);
        }
        
        @Override
        public java.util.Collection<String> getHeaders(String name) {
            String value = headers.get(name);
            return value != null ? java.util.Collections.singletonList(value) : java.util.Collections.emptyList();
        }
        
        @Override
        public java.util.Collection<String> getHeaderNames() {
            return headers.keySet();
        }
        
        @Override
        public String getCharacterEncoding() {
            return "UTF-8";
        }
        
        @Override
        public java.io.PrintWriter getWriter() throws IOException {
            return new java.io.PrintWriter(buffer);
        }
        
        @Override
        public void setCharacterEncoding(String charset) {}
        
        @Override
        public void setContentLengthLong(long len) {
            this.contentLength = (int)len;
        }
        
        @Override
        public String getContentType() {
            return contentType;
        }
        
        @Override
        public void setLocale(java.util.Locale loc) {}
        
        @Override
        public java.util.Locale getLocale() {
            return java.util.Locale.getDefault();
        }
        
        @Override
        public void flushBuffer() throws IOException {}
        
        @Override
        public boolean isCommitted() {
            return false;
        }
        
        @Override
        public void resetBuffer() {}
        
        @Override
        public void setBufferSize(int size) {
            this.bufferSize = size;
        }
        
        @Override
        public int getBufferSize() {
            return bufferSize;
        }
    }

    // This method was unused in the original code, keeping it for reference
    private String sanitizeHtml(String html) {
        org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(html);
        doc.outputSettings(new org.jsoup.nodes.Document.OutputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.html));
        return doc.body().html();
    }
}
