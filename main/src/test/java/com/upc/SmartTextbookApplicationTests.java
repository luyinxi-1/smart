package com.upc;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.modular.client.entity.User;
import com.upc.modular.client.mapper.UserMapper;
import com.upc.modular.client.service.IUserService;
import com.upc.modular.common.WordConversionPageService;
import com.upc.modular.common.WordConversionService;
import com.upc.modular.student.entity.Student;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Entities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SmartTextbookApplicationTests {
//    @BeforeAll
//    public static void init() {
////        System.out.println("当前 db.path：" + System.getProperty("db.path"));
//        System.setProperty("db.path", "C:\\Users\\Administrator\\.SmartTextbook\\client.db");
////        System.out.println("当前 db.path：" + System.getProperty("db.path"));
//    }

//    @Autowired
//    private IUserService userService;


//    @Autowired
//    private RedisTemplate redisTemplate;
//    @Autowired
//    private WordConversionService conversionService;
//    @Autowired
//    private WordConversionPageService conversionPageService;
//    @Autowired
//    private TextbookCatalogMapper textbookCatalogMapper;
//
//    @Test
//    void test1() {
//        String key = "student:101";
//        Student student = new Student();
//        student.setName("zhangsan");
//        student.setEmail("13211563@qq.com");
//        student.setPhone("13245687511");
//
//        // 2. 将 Student 对象存入 Redis
//        // Jackson 会自动将其转换为 JSON 字符串
//        System.out.println("正在存入 Student 对象: " + student);
//        redisTemplate.opsForValue().set(key, student);
//        System.out.println("存入成功！");
//
//        // 3. 从 Redis 中取出数据
//        System.out.println("正在读取 Student 对象...");
//        Object retrievedObject = redisTemplate.opsForValue().get(key);
//        System.out.println("读取到的原始对象: " + retrievedObject);
//        System.out.println("原始对象的类型: " + retrievedObject.getClass().getName());
//
//        // 4. 将取出的对象转换回 Student 类型
//        // 注意：因为 RedisTemplate<Object, Object> 的泛型是 Object，
//        // Jackson 默认会把 JSON 对象反序列化成一个 LinkedHashMap。
//        // 我们需要手动将这个 Map 转换成我们期望的 Student 对象。
//        if (retrievedObject instanceof Student) {
//            // 如果序列化器配置得非常好（比如使用了 GenericJackson2JsonRedisSerializer 并处理了类型信息），可能直接就是 Student 类型
//            System.out.println("直接是 Student 类型！" + retrievedObject);
//        } else if (retrievedObject instanceof java.util.Map) {
//            System.out.println("需要手动序列化");
//        }
//
//        System.out.println("====== 测试结束 ======");
//
//    }
//
//    @Test
//    void test2() {
//        System.out.println("--- Conversion Demo Runner is executing ---");
//
//        // --- 请修改为您自己的文件路径 ---
//        String inputPath = "C:\\Users\\HP\\Desktop\\详细设计说明书v6.0.docx";
//        String outputPath = "C:\\Users\\HP\\Desktop\\详细设计说明书v6.0.html";
//        // --------------------------------
//
//        // 调用服务执行转换
//        conversionService.convertWordToHtml(inputPath, outputPath);
//
//        System.out.println("--- Conversion Demo Runner has finished ---");
//    }
//
//    @Test
//    void convertHtmlToWord() {
//        try {
//            System.out.println("--- HTML → Word 转换开始 ---");
//
//            // --- 请替换为你自己的 HTML 路径 ---
//            String htmlPath = "C:\\Users\\HP\\Desktop\\详细设计说明书v6.0.html";
//            String docxPath = "C:\\Users\\HP\\Desktop\\bac2.docx";
//
//            // 读取 HTML 并构建 Word 文档
//            Document doc = new Document(htmlPath);
//
//            // 保存为 DOCX 格式
//            doc.save(docxPath, SaveFormat.DOCX);
//
//            System.out.println("✅ 转换完成！文件保存到：" + docxPath);
//        } catch (Exception e) {
//            System.err.println("❌ 转换失败！");
//            e.printStackTrace();
//        }
//    }
////    @Test
////    public void mergeHtmlFragmentsToWord() {
////        try {
////            LambdaQueryWrapper<TextbookCatalog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
////            lambdaQueryWrapper.eq(TextbookCatalog::getTextbookId, 1L);
////            lambdaQueryWrapper.orderByAsc(TextbookCatalog::getSort);
////            List<TextbookCatalog> textbookCatalogs = textbookCatalogMapper.selectList(lambdaQueryWrapper);
////            List<String> htmlFragments = textbookCatalogs.stream()
////                    .sorted(Comparator.comparing(TextbookCatalog::getSort))
////                    .flatMap(catalog -> Stream.of(catalog.getCatalogName(), catalog.getContent()))
////                    .filter(Objects::nonNull)
////                    .collect(Collectors.toList());
////            String outputDocxPath = "C:\\Users\\HP\\Desktop\\recovered.docx";
////            // 1. 拼接HTML结构
////            StringBuilder htmlBuilder = new StringBuilder();
////            htmlBuilder.append("<html><head><meta charset='UTF-8'></head><body>");
////
////            for (String fragment : htmlFragments) {
////                htmlBuilder.append(fragment).append("\n");
////            }
////
////            htmlBuilder.append("</body></html>");
////
////            // 2. 转义检查（防止 <body> 冲突）
////            String mergedHtml = sanitizeHtml(htmlBuilder.toString());
////
////            // 3. 保存临时HTML文件（可选）
////            String tempHtmlPath = "temp_merged.html";
////            Files.write(Paths.get(tempHtmlPath), mergedHtml.getBytes(StandardCharsets.UTF_8));
////
////            // 4. 加载并保存为 Word 文档
////            Document doc = new Document(tempHtmlPath);
////            doc.save(outputDocxPath, SaveFormat.DOCX);
////
////            System.out.println("✅ 合并并生成 Word 成功: " + outputDocxPath);
////
////        } catch (Exception e) {
////            System.err.println("❌ 合并 HTML 转 Word 失败！");
////            e.printStackTrace();
////        }
////    }
//
//    @Test
//    public void mergeHtmlFragmentsToWord() {
//        try {
//            LambdaQueryWrapper<TextbookCatalog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//            lambdaQueryWrapper.eq(TextbookCatalog::getTextbookId, 1L);
//            lambdaQueryWrapper.orderByAsc(TextbookCatalog::getSort);
//
//            List<TextbookCatalog> textbookCatalogs = textbookCatalogMapper.selectList(lambdaQueryWrapper);
//
//            // 拼接 HTML 片段（标题 + 正文）
//            List<String> htmlFragments = textbookCatalogs.stream()
//                    .flatMap(catalog -> Stream.of(catalog.getCatalogName(), catalog.getContent()))
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//
//            // 拼接 HTML 主体
//            StringBuilder htmlBuilder = new StringBuilder();
//            htmlBuilder.append("<html><head><meta charset='UTF-8'></head><body>");
//
//            for (String fragment : htmlFragments) {
//                htmlBuilder.append(fragment).append("\n");
//            }
//
//            htmlBuilder.append("</body></html>");
//
//            String mergedHtml = htmlBuilder.toString(); // 可选 sanitizeHtml(htmlBuilder.toString());
//
//            // 保存合并的 HTML 到桌面临时文件
//            String tempHtmlPath = "C:\\Users\\HP\\Desktop\\temp_merged.html";
//            Files.write(Paths.get(tempHtmlPath), mergedHtml.getBytes(StandardCharsets.UTF_8));
//
////            // 保存合并的 HTML 片段作为桌面上的独立文件
////            String tempHtmlFragmentsPath = "C:\\Users\\HP\\Desktop\\temp_html_fragments.html";
////            Files.write(Paths.get(tempHtmlFragmentsPath), mergedHtml.getBytes(StandardCharsets.UTF_8));
//
////            System.out.println("临时 HTML 文件已保存: " + tempHtmlFragmentsPath);
//
//            // 导入 Aspose 文档并保存为 Word 到桌面
//            String outputDocxPath = "C:\\Users\\HP\\Desktop\\bac2.docx";
//            Document doc = new Document(tempHtmlPath);
//            doc.save(outputDocxPath, SaveFormat.DOCX);
//
//            System.out.println("✅ 合并并生成 Word 成功: " + outputDocxPath);
//        } catch (Exception e) {
//            System.err.println("❌ 合并 HTML 转 Word 失败！");
//            e.printStackTrace();
//        }
//    }
//
//
//
//
//    private String sanitizeHtml(String html) {
//        // 使用 Jsoup 清洗可能嵌套的 body/head 等结构
//        org.jsoup.nodes.Document doc = Jsoup.parse(html);
//        doc.outputSettings(new org.jsoup.nodes.Document.OutputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.html).escapeMode(Entities.EscapeMode.xhtml));
//        return doc.html();
//    }
//
//    @Test
//    public void test03() {
//        System.out.println(userService.list());
//    }

}
