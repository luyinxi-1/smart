package com.upc.modular.textbook.service.impl;

import com.aspose.words.SaveFormat;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.param.TextbookCatalogDto;
import com.upc.modular.textbook.param.WordRequest;
import com.upc.modular.textbook.service.ITextbookCatalogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.utils.Word2HtmlUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-08
 */
@Service
public class TextbookCatalogServiceImpl extends ServiceImpl<TextbookCatalogMapper, TextbookCatalog> implements ITextbookCatalogService {

    @Autowired
    private TextbookMapper textbookMapper;

    @Autowired
    private TextbookCatalogMapper textbookCatalogMapper;
    /**
     * 将HTML内容解析为具有层级结构的TextbookCatalog对象列表
     * @param htmlContent 从Word转换来的HTML字符串
     * @return 解析后的对象列表
     */
    public List<TextbookCatalogDto> parseHtmlToCatalogs(String htmlContent) {
        List<TextbookCatalogDto> resultList = new ArrayList<>();
        Document doc = Jsoup.parse(htmlContent);

        // 使用一个数组作为“父级栈”来跟踪每一级的最后一个目录项
        // 索引1对应1级标题，索引2对应2级标题，以此类推
        TextbookCatalogDto[] parentStack = new TextbookCatalogDto[5];

        // 排序计数器，每次增加100来实现稀疏排序
        int sortCounter = 0;

        // 当前正在处理的目录项
        TextbookCatalogDto currentCatalog = null;

        // 获取<body>下的所有直接子元素，这能保证遍历顺序和文档流一致
        Elements bodyChildren = doc.select("body > div > *");

        for (Element element : bodyChildren) {
            String tagName = element.tagName().toLowerCase();

            // 正则表达式，判断标签是否为h1, h2, h3, h4
            Pattern headingPattern = Pattern.compile("h([1-4])");
            Matcher matcher = headingPattern.matcher(tagName);

            if (matcher.matches()) {
                // ---- 情况一：当前元素是标题 ----

                // 1. 如果之前有正在处理的目录项，说明它的内容部分已收集完毕，可以正式加入结果列表
                if (currentCatalog != null) {
                    resultList.add(currentCatalog);
                }

                // 2. 创建一个新的目录项来代表这个标题
                currentCatalog = new TextbookCatalogDto();

                // 3. 判断并设置目录级别 (catalog_level)
                int level = Integer.parseInt(matcher.group(1)); // 从"h1"中提取出1
                currentCatalog.setCatalogLevel(level);

                // 4. 设置目录名 (catalog_name)，包含HTML标签
                currentCatalog.setCatalogName(element.outerHtml());

                // 5. 设置排序 (sort)，使用稀疏的步长
                sortCounter += 100;
                currentCatalog.setSort(sortCounter);

                // 6. 判断并设置父级关系 (father_catalog_id)
                // 找到当前级别上一级的父对象
                if (level > 1) {
                    TextbookCatalogDto parent = parentStack[level - 1];
                    currentCatalog.setParent(parent); // 在内存中建立引用
                    if (parent != null) {
                        // 注意：此时父对象的真实数据库ID可能还不知道
                        // father_catalog_id 的最终赋值将在保存到数据库时处理
                        currentCatalog.setFatherCatalogId(parent.getId());
                    } else {
                        currentCatalog.setFatherCatalogId(0L);
                    }
                } else {
                    // 1级目录的父ID为0
                    currentCatalog.setFatherCatalogId(0L);
                }

                // 7. 更新“父级栈”，将当前目录项作为未来子级的父级
                parentStack[level] = currentCatalog;

                // 将更深级别的父级清空，避免错误的父子关系
                // 例如，遇到一个新的h2，那么之前记录的h3和h4父级就失效了
                for (int i = level + 1; i < parentStack.length; i++) {
                    parentStack[i] = null;
                }

            } else {
                // ---- 情况二：当前元素是正文内容 (p, table, ul, div等) ----

                // 如果当前有正在处理的目录项，就将这个元素作为其正文内容
                if (currentCatalog != null) {
                    // 拼接HTML内容，包含标签
                    String existingContent = currentCatalog.getContent() == null ? "" : currentCatalog.getContent();
                    currentCatalog.setContent(existingContent + element.outerHtml());
                }
            }
        }

        // 循环结束后，不要忘记添加最后一个正在处理的目录项
        if (currentCatalog != null && !resultList.contains(currentCatalog)) {
            resultList.add(currentCatalog);
        }

        return resultList;
    }


    @Override
    public void processAndSaveHtml(MultipartFile file, Long textbookId) {
        try {
            // 1. 将文件转换为HTML字符串
            String htmlString = Word2HtmlUtils.toHtmlString(file);

            // 2. 从HTML解析出带层级关系的对象列表
            List<TextbookCatalogDto> catalogs = this.parseHtmlToCatalogs(htmlString);

            // 3. 调用递归方法保存这个列表
            saveCatalogTree(catalogs, null, textbookId);
        } catch (IOException e) {
            throw new RuntimeException("文件转换失败", e);  // 可以根据需求抛出自定义异常
        } catch (Exception e) {
            throw new RuntimeException("处理HTML和保存目录时发生异常", e);  // 可以根据需求抛出自定义异常
        }
    }

    @Override
    public Boolean insert(TextbookCatalog param) {
        if (ObjectUtils.isEmpty(param) || ObjectUtils.isEmpty(param.getTextbookId()) || ObjectUtils.isEmpty(param.getSort()) || ObjectUtils.isEmpty(param.getFatherCatalogId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        return this.save(param);
    }

    @Override
    public Boolean delete(Long id) {
        if (ObjectUtils.isEmpty(id)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        return this.removeById(id);
    }

    @Override
    public Boolean updateTextbook(TextbookCatalog param) {
        if (ObjectUtils.isEmpty(param)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        return this.updateById(param);
    }

    @Override
    public void exportTextbook(HttpServletResponse response, Long textbookId) {
        if (ObjectUtils.isEmpty(textbookId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        try {
            Textbook textbook = textbookMapper.selectById(textbookId);
            // 查询目录
            LambdaQueryWrapper<TextbookCatalog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TextbookCatalog::getTextbookId, textbookId);
            lambdaQueryWrapper.orderByAsc(TextbookCatalog::getSort);

            List<TextbookCatalog> textbookCatalogs = textbookCatalogMapper.selectList(lambdaQueryWrapper);

            // 构造 HTML 内容
            List<String> htmlFragments = textbookCatalogs.stream()
                    .flatMap(catalog -> Stream.of(catalog.getCatalogName(), catalog.getContent()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta charset='UTF-8'></head><body>");
            for (String fragment : htmlFragments) {
                htmlBuilder.append(fragment).append("\n");
            }
            htmlBuilder.append("</body></html>");
            String mergedHtml = htmlBuilder.toString();

            // 将 HTML 转为 Word
            try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
                com.aspose.words.Document doc = new com.aspose.words.Document(new ByteArrayInputStream(mergedHtml.getBytes(StandardCharsets.UTF_8)));
                doc.save(outStream, SaveFormat.DOCX);

                // 设置响应头
                response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

                String fileName = textbook.getTextbookName() + ".docx";
                String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

                response.setContentLength(outStream.size());

                // 写入响应流
                OutputStream responseOutputStream = response.getOutputStream();
                outStream.writeTo(responseOutputStream);
                responseOutputStream.flush();
            }
        } catch (Exception e) {
            System.err.println("❌ 导出 Word 出错！");
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "导出 Word 失败");
        }
    }

    @Override
    public void exportTextbookByString(HttpServletResponse response, String html) {
        if (ObjectUtils.isEmpty(html)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "HTML内容不能为空");
        }

        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            // 将 HTML 内容转为 Word 文档
            com.aspose.words.Document doc = new com.aspose.words.Document(
                    new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8))
            );
            doc.save(outStream, SaveFormat.DOCX);

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            String fileName = "textbook.docx";
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            response.setContentLength(outStream.size());

            // 写入响应流
            OutputStream responseOutputStream = response.getOutputStream();
            outStream.writeTo(responseOutputStream);
            responseOutputStream.flush();
        } catch (Exception e) {
            System.err.println("❌ HTML导出Word出错！");
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "HTML导出Word失败");
        }
    }



    /**
     * 递归或循环分层保存目录树
     * @param allCatalogs 所有解析出的目录项
     * @param parent 正在处理的父节点（首次调用为null）
     * @param textbookId 教材ID
     */
    private void saveCatalogTree(List<TextbookCatalogDto> allCatalogs, TextbookCatalogDto parent, Long textbookId) {
        Long parentId = (parent == null) ? 0L : parent.getId();

        // 筛选出当前层级的所有子节点
        List<TextbookCatalogDto> children = new ArrayList<>();
        for (TextbookCatalogDto catalog : allCatalogs) {
            if (catalog.getParent() == parent) {
                children.add(catalog);
            }
        }

        // 遍历并保存当前层级的节点
        for (TextbookCatalogDto child : children) {
            // 设置从属关系和父ID
            child.setTextbookId(textbookId);
            child.setFatherCatalogId(parentId);

            // 保存到数据库，MyBatis会自动将生成的自增ID回填到child对象中
            this.save(child);

            // 递归保存当前节点的子节点
            saveCatalogTree(allCatalogs, child, textbookId);
        }
    }
}
