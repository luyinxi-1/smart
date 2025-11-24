package com.upc.modular.textbook.service.impl;

import com.aspose.words.HtmlLoadOptions;
import com.aspose.words.PdfSaveOptions;
import com.aspose.words.PdfTextCompression;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.aspose.words.SaveFormat;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.materials.entity.Attachment;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.service.IAttachmentService;
import com.upc.modular.materials.entity.MaterialsTextbookMapping;
import com.upc.modular.materials.service.IMaterialsTextbookMappingService;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.upc.modular.questionbank.service.ITeachingQuestionBankService;
import com.upc.modular.teachingactivities.entity.DiscussionTopic;
import com.upc.modular.teachingactivities.service.IDiscussionTopicService;
import com.upc.modular.textbook.entity.IdeologicalMaterial;
import com.upc.modular.textbook.entity.LearningAnnotationsAndLabels;
import com.upc.modular.textbook.service.IIdeologicalMaterialService;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.param.ReadTextbookReturnParam;
import com.upc.modular.textbook.param.TextbookCatalogDto;
import com.upc.modular.textbook.param.TextbookCatalogInsertParam;
import com.upc.modular.textbook.param.TextbookTree;
import com.upc.modular.textbook.service.ILearningAnnotationsAndLabelsService;
import com.upc.modular.textbook.service.ITextbookCatalogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.service.ITextbookService;
import com.upc.utils.Word2HtmlUtils;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
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
    @Resource
    private ITextbookService textbookService;
    @Autowired
    private TextbookCatalogMapper textbookCatalogMapper;
    @Autowired
    private ILearningAnnotationsAndLabelsService labelsService;

    @Autowired
    private SysUserMapper sysUserMapper;
    
    // 注入相关服务
    @Autowired
    private IAttachmentService attachmentService;
    
    @Autowired
    private ITeachingQuestionBankService teachingQuestionBankService;
    
    @Autowired
    @Lazy
    private IDiscussionTopicService discussionTopicService;
    
    @Autowired
    @Lazy
    private IIdeologicalMaterialService ideologicalMaterialService;
    
    @Autowired
    private IMaterialsTextbookMappingService materialsTextbookMappingService;

    @Autowired
    private ITeachingMaterialsService teachingMaterialsService;

    @Override
    public void processAndSaveHtml(MultipartFile file, Long textbookId) {
        if (file.isEmpty() || textbookId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        try {
            // 1. 将文件转换为HTML字符串
            String htmlString = Word2HtmlUtils.toHtmlString(file, textbookId);

            // 1.5 将第一个标题之前的Html内容取出来
            String preHtmlString = extractAllHtmlBeforeFirstHeading(htmlString);
            if (StringUtils.isNotBlank(preHtmlString)) {
                textbookService.update(new LambdaUpdateWrapper<Textbook>()
                        .set(Textbook::getH5HeadCode, preHtmlString)
                        .eq(Textbook::getId, textbookId));
            }

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

    private static final int INITIAL_SORT = 100;
    private static final int STEP = 100;
    private static final int MAX_LEVEL = 4;
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean insert(List<TextbookCatalogInsertParam> params) {
        if (params == null || params.isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        // 统一教材ID & firstAdd
        Long textbookId = params.get(0).getTextbookId();
        Integer firstAdd = params.get(0).getFirstAdd();
        if (textbookId == null || firstAdd == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "textbookId / firstAdd 不能为空");
        }
        // 所有条目的 textbookId/firstAdd 必须一致
        for (TextbookCatalogInsertParam p : params) {
            if (!Objects.equals(textbookId, p.getTextbookId()) || !Objects.equals(firstAdd, p.getFirstAdd())) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "批次中的 textbookId/firstAdd 必须一致");
            }
        }

        if (firstAdd == 0) {
            // ===== 场景1：首次整书导入 =====
            return insertFirstAdd(params, textbookId);
        } else {
            // ===== 场景2：增量插入 =====
            return insertIncremental(params, textbookId);
        }
    }

    /** 场景1：表空时整书导入（顺序传入），按 100 递增，处理临时ID映射 */
    private Boolean insertFirstAdd(List<TextbookCatalogInsertParam> params, Long textbookId) {
        // 校验表确实为空（同一本书）
        Long count = lambdaQuery().eq(TextbookCatalog::getTextbookId, textbookId).count();
        if (count != null && count > 0) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "firstAdd=0 但数据库已有该书数据");
        }

        Map<String, Long> tempId2RealId = new HashMap<>();
        int sort = INITIAL_SORT;

        for (TextbookCatalogInsertParam p : params) {
            TextbookCatalog entity = new TextbookCatalog();
            entity.setTextbookId(textbookId);
            entity.setCatalogName(p.getCatalogName());
            entity.setContent(p.getContent());
            entity.setCatalogLevel(p.getCatalogLevel());

            // 父ID：优先真实 fatherCatalogId；否则看 temporaryParentId 映射；顶级则设 0
            Long realFatherId = p.getFatherCatalogId();
            if (realFatherId == null || realFatherId == 0) {
                if (StringUtils.isNotBlank(p.getTemporaryParentId())) {
                    realFatherId = tempId2RealId.getOrDefault(p.getTemporaryParentId(), 0L);
                } else {
                    realFatherId = 0L;
                }
            }
            entity.setFatherCatalogId(realFatherId);

            entity.setSort(sort);
            sort += STEP;

            entity.setCatalogUuid(p.getCatalogUuid());
            // 插入
            textbookCatalogMapper.insert(entity);


            if (StringUtils.isNotBlank(p.getCatalogUuid())) {
                tempId2RealId.put(p.getCatalogUuid(), entity.getId());
            }
        }
        return true;
    }

    /** 场景2：增量插入 */
    private Boolean insertIncremental(List<TextbookCatalogInsertParam> params, Long textbookId) {
        // 缓存"临时ID → 真实ID"映射（本次批内引用）
        Map<String, Long> tempId2RealId = new HashMap<>();

        for (TextbookCatalogInsertParam p : params) {
            TextbookCatalog entity = new TextbookCatalog();
            entity.setTextbookId(textbookId);
            entity.setCatalogName(p.getCatalogName());
            entity.setContent(p.getContent());
            entity.setCatalogLevel(p.getCatalogLevel());
            entity.setCatalogUuid(p.getCatalogUuid());

            // 解析父ID：先用真实 fatherCatalogId；若为 0/空，再看 temporaryParentId
            Long fatherId = normalizeFatherId(p, tempId2RealId);
            entity.setFatherCatalogId(fatherId);

            // ===== 关键修改：解析同级章节ID =====
            // 目标：根据前端传入的 sameCatalogLevelId 或 sameCatalogLevelUuid，解析出用于计算排序的、真实的同级节点ID
            Long resolvedSameCatalogLevelId = null;
            if (StringUtils.isNotBlank(p.getSameCatalogLevelUuid())) {
                // 场景一：前端传入了同级节点的临时UUID，说明该同级节点是本次批量插入的。
                // 从 tempId2RealId 映射中查找它已经存入数据库的真实ID。
                resolvedSameCatalogLevelId = tempId2RealId.get(p.getSameCatalogLevelUuid());
                // 做一个健壮性检查，如果找不到，说明前端传的数据或顺序有问题。
                if (resolvedSameCatalogLevelId == null) {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "同级临时UUID无效或顺序错误: " + p.getSameCatalogLevelUuid());
                }
            } else {
                // 场景二：前端传入的是已存在的数据库ID，直接使用。
                resolvedSameCatalogLevelId = p.getSameCatalogLevelId();
            }

            // 计算 sort
            Integer sort = computeSortForIncrement(textbookId, fatherId, resolvedSameCatalogLevelId);
            entity.setSort(sort);

            // 插入
            textbookCatalogMapper.insert(entity);

            // 建立批内临时ID映射（供后续条目引用）
            if (StringUtils.isNotBlank(p.getCatalogUuid())) {
                tempId2RealId.put(p.getCatalogUuid(), entity.getId());
            }
        }
        return true;
    }

    private Long normalizeFatherId(TextbookCatalogInsertParam p, Map<String, Long> tempId2RealId) {
        Long fatherId = p.getFatherCatalogId();
        if (fatherId != null && fatherId > 0) return fatherId;

        if (StringUtils.isNotBlank(p.getTemporaryParentId())) {
            return tempId2RealId.getOrDefault(p.getTemporaryParentId(), 0L);
        }
        return 0L; // 顶级
    }

    /** 计算增量插入时的 sort（含"同级后插"与"该父级下第一个"两种） */
    private Integer computeSortForIncrement(Long textbookId, Long fatherId, Long sameCatalogLevelId) {
        // 左边界 L
        int L;
        if (sameCatalogLevelId != null && sameCatalogLevelId > 0) {
            // 插在"上一个同级目录"的整棵子树之后
            L = maxSortInSubtree(textbookId, sameCatalogLevelId);
        } else {
            // 插在该父级下第一个 → 取父目录的 sort（顶级父=0）
            if (fatherId == null || fatherId == 0) {
                L = 0;
            } else {
                TextbookCatalog parent = textbookCatalogMapper.selectById(fatherId);
                if (parent == null) {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "父目录不存在：" + fatherId);
                }
                L = parent.getSort() == null ? 0 : parent.getSort();
            }
        }

        // 右边界 R：同一本书中"最接近且更大"的 sort
        Integer R = minGreaterSort(textbookId, L);

        Integer candidate;
        if (R == null) {
            // 末尾插入
            candidate = L + STEP;
        } else {
            long gap = (long) R - L;
            if (gap <= 1) {
                // 无间隙 → 重排整书（或重排同一父级的子树），这里给出整书重排实现
                reindexTextbook(textbookId);
                // 重排后再取一次
                R = minGreaterSort(textbookId, L);
                if (R == null) {
                    candidate = L + STEP;
                } else {
                    candidate = L + (R - L) / 2;
                }
            } else {
                candidate = L + (R - L) / 2;
            }
        }
        // 防御：至少比 L 大 1
        if (candidate <= L) candidate = L + 1;
        return candidate;
    }

    /** 同一本书里，所有节点中，> base 的最小 sort（没有则返回 null） */
    private Integer minGreaterSort(Long textbookId, int baseSortExclusive) {
        return lambdaQuery()
                .eq(TextbookCatalog::getTextbookId, textbookId)
                .gt(TextbookCatalog::getSort, baseSortExclusive)
                .orderByAsc(TextbookCatalog::getSort)
                .last("LIMIT 1")
                .oneOpt()
                .map(TextbookCatalog::getSort)
                .orElse(null);
    }

    /** 取某节点"整棵子树"的最大 sort（含自身）。深度最多 4 层。 */
    private int maxSortInSubtree(Long textbookId, Long rootId) {
        TextbookCatalog root = textbookCatalogMapper.selectById(rootId);
        if (root == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "sameCatalogLevelId 无效：" + rootId);
        }
        int max = root.getSort() == null ? 0 : root.getSort();

        // BFS/DFS 递归查子代（最多 4 层）
        Deque<Long> q = new ArrayDeque<>();
        Map<Long, Integer> levelMap = new HashMap<>();
        q.add(rootId);
        levelMap.put(rootId, 1);

        while (!q.isEmpty()) {
            Long cur = q.poll();
            int lv = levelMap.get(cur);
            if (lv >= MAX_LEVEL) continue;

            List<TextbookCatalog> children = lambdaQuery()
                    .eq(TextbookCatalog::getTextbookId, textbookId)
                    .eq(TextbookCatalog::getFatherCatalogId, cur)
                    .list();
            for (TextbookCatalog c : children) {
                if (c.getSort() != null) {
                    max = Math.max(max, c.getSort());
                }
                q.add(c.getId());
                levelMap.put(c.getId(), lv + 1);
            }
        }
        return max;
    }

    /** 给整本书重排 sort：100,200,300,...（保持当前 sort 顺序的相对次序） */
    private void reindexTextbook(Long textbookId) {
        List<TextbookCatalog> all = lambdaQuery()
                .eq(TextbookCatalog::getTextbookId, textbookId)
                .orderByAsc(TextbookCatalog::getSort)
                .list();

        int s = INITIAL_SORT;
        for (TextbookCatalog x : all) {
            x.setSort(s);
            s += STEP;
        }
        this.updateBatchById(all);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(IdParam idParam) {
        if (ObjectUtils.isEmpty(idParam) || ObjectUtils.isEmpty(idParam.getIdList())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传入的ID列表不能为空");
        }
        List<Long> ids = idParam.getIdList();
        Set<Long> allIdsToDelete = new HashSet<>(ids);

        List<Long> parentIds = new ArrayList<>(ids);

        while (!parentIds.isEmpty()) {

            List<Long> childIds = this.list(
                            new MyLambdaQueryWrapper<TextbookCatalog>()
                                    .select(TextbookCatalog::getId) // 只查询ID字段，提高效率
                                    .in(TextbookCatalog::getFatherCatalogId, parentIds)
                    ).stream()
                    .map(TextbookCatalog::getId)
                    .collect(Collectors.toList());

            // d. 如果没有找到子节点，说明已经到达树的末端，退出循环
            if (childIds.isEmpty()) {
                break;
            }

            // e. 将新找到的子节点ID加入待删除的总集合
            allIdsToDelete.addAll(childIds);

            // f. 将子节点作为下一轮的父节点，继续向下查找
            parentIds = childIds;
        }

        // 级联删除与这些章节关联的数据
        // 1. 删除附件 (Attachment)
        LambdaQueryWrapper<Attachment> attachmentQueryWrapper = new LambdaQueryWrapper<>();
        attachmentQueryWrapper.in(Attachment::getObjectId, allIdsToDelete)
                .eq(Attachment::getObjectType, "textbook_catalog");
        attachmentService.remove(attachmentQueryWrapper);
        
        // 2. 清空题库中的章节ID字段 (TeachingQuestionBank)
        LambdaQueryWrapper<TeachingQuestionBank> questionBankQueryWrapper = new LambdaQueryWrapper<>();
        questionBankQueryWrapper.in(TeachingQuestionBank::getTextbookCatalogId, allIdsToDelete);
        TeachingQuestionBank updateQuestionBank = new TeachingQuestionBank();
        updateQuestionBank.setTextbookCatalogId(null);
        teachingQuestionBankService.update(updateQuestionBank, questionBankQueryWrapper);
        
        // 3. 删除教学活动 (DiscussionTopic)
        LambdaQueryWrapper<DiscussionTopic> discussionTopicQueryWrapper = new LambdaQueryWrapper<>();
        discussionTopicQueryWrapper.in(DiscussionTopic::getTextbookCatalogId, allIdsToDelete);
        discussionTopicService.remove(discussionTopicQueryWrapper);
        
        // 4. 删除教学思政 (IdeologicalMaterial)
        LambdaQueryWrapper<IdeologicalMaterial> ideologicalMaterialQueryWrapper = new LambdaQueryWrapper<>();
        ideologicalMaterialQueryWrapper.in(IdeologicalMaterial::getTextbookCatalogId, allIdsToDelete);
        ideologicalMaterialService.remove(ideologicalMaterialQueryWrapper);
        
        // 5. 删除教材素材映射关系 (MaterialsTextbookMapping)
        LambdaQueryWrapper<MaterialsTextbookMapping> mappingQueryWrapper = new LambdaQueryWrapper<>();
        mappingQueryWrapper.in(MaterialsTextbookMapping::getChapterId, allIdsToDelete);
        materialsTextbookMappingService.remove(mappingQueryWrapper);

        // 3. 批量删除所有收集到的ID
        // 注意：MyBatis-Plus的批量删除方法是 removeByIds
        return this.removeByIds(allIdsToDelete);
    }

    @Override
    @Transactional
    public Boolean updateTextbook(List<TextbookCatalog> param) {
        if (ObjectUtils.isEmpty(param)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        return this.updateBatchById(param);
    }

    @Override
    public void exportTextbook(HttpServletResponse response, Long textbookId, String baseUrl) {
        if (ObjectUtils.isEmpty(textbookId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        try {
            Textbook textbook = textbookMapper.selectById(textbookId);
            if (textbook == null) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "未找到对应教材");
            }

            // 查询目录
            LambdaQueryWrapper<TextbookCatalog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TextbookCatalog::getTextbookId, textbookId);
            lambdaQueryWrapper.orderByAsc(TextbookCatalog::getSort);

            List<TextbookCatalog> textbookCatalogs = textbookCatalogMapper.selectList(lambdaQueryWrapper);

            // 构造 HTML 内容
            StringBuilder htmlBuilder = new StringBuilder();
            String h5HeadCode = textbook.getH5HeadCode();
            htmlBuilder.append(h5HeadCode == null ? "" : h5HeadCode);

            // 保持原来的拼接方式
            List<String> htmlFragments = textbookCatalogs.stream()
                    .flatMap(catalog -> Stream.of(catalog.getCatalogName(), catalog.getContent()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            for (String fragment : htmlFragments) {
                htmlBuilder.append(fragment).append("\n");
            }
            htmlBuilder.append("</body></html>");

            // 原始 HTML
            String mergedHtml = htmlBuilder.toString();

            // 处理掉 file-div1 这类“附件块”
            mergedHtml = processFileDivBlocks(mergedHtml, baseUrl);

            // 将 HTML 转为 Word
            try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
                HtmlLoadOptions loadOptions = new HtmlLoadOptions();
                loadOptions.setEncoding(StandardCharsets.UTF_8);

                com.aspose.words.Document doc = new com.aspose.words.Document(
                        new ByteArrayInputStream(mergedHtml.getBytes(StandardCharsets.UTF_8)), loadOptions);
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

    private String processFileDivBlocks(String html, String baseUrl) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        Document doc = Jsoup.parse(html);
        Elements fileDivs = doc.select("div[type=file-div1]");

        if (fileDivs.isEmpty()) {
            return doc.outerHtml();
        }

        // 1. 在第一个附件块前插一个“二维码表格容器”
        Element firstFileDiv = fileDivs.first();

        Element tableWrapper = doc.createElement("div");
        tableWrapper.attr("style", "width:100%;text-align:center;margin:10px 0;");

        Element table = doc.createElement("table");
        // 让表格整体居中，边距用 padding/padding
        table.attr("style", "margin:0 auto;border-collapse:collapse;");

        tableWrapper.appendChild(table);
        firstFileDiv.before(tableWrapper);

        int maxCols = 3;      // 一行最多几个二维码，可以自己调
        int colIndex = 0;
        Element currentRow = null;
        boolean hasAnyQr = false;

        // 2. 遍历所有附件块
        for (Element fileDiv : fileDivs) {

            String idStr = fileDiv.attr("id");
            String fileUrl = null;
            String displayName = null; // 二维码下面显示的名字

            // 2.1 根据 id 查素材，拼 fileUrl，并取名称
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    Long materialId = Long.valueOf(idStr.trim());
                    TeachingMaterials tm = teachingMaterialsService.getById(materialId);
                    if (tm != null && tm.getFilePath() != null && !tm.getFilePath().trim().isEmpty()) {

                        String path = tm.getFilePath().trim();

                        // 拼接 URL
                        if (path.startsWith("http")) {
                            fileUrl = path;  // 已是完整 URL
                        } else {
                            if (baseUrl.endsWith("/") && path.startsWith("/")) {
                                fileUrl = baseUrl + path.substring(1);
                            } else if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
                                fileUrl = baseUrl + "/" + path;
                            } else {
                                fileUrl = baseUrl + path;
                            }
                        }

                        // 显示名称：优先 name，其次 fileName
                        if (tm.getName() != null && !tm.getName().trim().isEmpty()) {
                            displayName = tm.getName().trim();
                        } else if (tm.getFileName() != null && !tm.getFileName().trim().isEmpty()) {
                            displayName = tm.getFileName().trim();
                        }
                    }
                } catch (NumberFormatException ignore) {
                }
            }

            // 2.2 找跟在附件块后面的灰色说明 span
            Element tailSpan = null;
            Node next = fileDiv.nextSibling();
            while (next instanceof TextNode && ((TextNode) next).text().trim().isEmpty()) {
                next = next.nextSibling();
            }
            if (next instanceof Element) {
                Element nextElem = (Element) next;
                if ("span".equalsIgnoreCase(nextElem.tagName())
                        && nextElem.attr("style").contains("font-family")
                        && nextElem.attr("style").contains("宋体")) {
                    tailSpan = nextElem;
                }
            }

            // 2.3 如果 fileUrl 无效 → 只做清理，不生成二维码
            if (fileUrl == null || fileUrl.isEmpty()) {
                cleanEmptyParagraphsAfter(fileDiv);
                fileDiv.remove();
                if (tailSpan != null) {
                    tailSpan.remove();
                }
                continue;
            }

            // 2.4 生成二维码
            String qrDataUrl = generateQrCodeDataUrl(fileUrl);
            if (qrDataUrl == null || qrDataUrl.isEmpty()) {
                cleanEmptyParagraphsAfter(fileDiv);
                fileDiv.remove();
                if (tailSpan != null) {
                    tailSpan.remove();
                }
                continue;
            }

            // 2.5 真正有二维码了，开始往 table 里塞卡片
            hasAnyQr = true;
            if (currentRow == null || colIndex >= maxCols) {
                currentRow = doc.createElement("tr");
                table.appendChild(currentRow);
                colIndex = 0;
            }

            Element td = doc.createElement("td");
            td.attr("style", "padding:10px;vertical-align:top;text-align:center;");

            // 卡片 div（方便将来想加边框啥的）
            Element cardDiv = doc.createElement("div");
            cardDiv.attr("style", "width:150px;margin:0 auto;");

            // 二维码图片
            Element qrImg = doc.createElement("img");
            qrImg.attr("src", qrDataUrl);
            qrImg.attr("alt", "附件二维码");
            qrImg.attr("style",
                    "width:120px;height:120px;display:block;margin:0 auto;");

            cardDiv.appendChild(qrImg);

            // 下方文件名
            if (displayName != null && !displayName.isEmpty()) {
                Element nameDiv = doc.createElement("div");
                nameDiv.attr("style",
                        "margin-top:5px;font-size:12px;line-height:1.4;" +
                                "word-wrap:break-word;word-break:break-all;");
                nameDiv.text(displayName);
                cardDiv.appendChild(nameDiv);
            }

            td.appendChild(cardDiv);
            currentRow.appendChild(td);
            colIndex++;

            // 2.6 清理原位置的 fileDiv + tailSpan + 空 p
            cleanEmptyParagraphsAfter(fileDiv);
            fileDiv.remove();
            if (tailSpan != null) {
                tailSpan.remove();
            }
        }

        // 如果一个二维码都没生成，说明 table 是空的，干脆把 wrapper 也删了
        if (!hasAnyQr) {
            tableWrapper.remove();
        }

        return doc.outerHtml();
    }

    /**
     * 清理某个节点后面紧挨着的空白文本和空 <p>，避免多余换行
     */
    private void cleanEmptyParagraphsAfter(Node node) {
        Node sib = node.nextSibling();
        while (sib != null) {
            if (sib instanceof TextNode && ((TextNode) sib).text().trim().isEmpty()) {
                Node rm = sib;
                sib = sib.nextSibling();
                rm.remove();
                continue;
            }
            if (sib instanceof Element) {
                Element e = (Element) sib;
                if ("p".equalsIgnoreCase(e.tagName()) && e.text().trim().isEmpty()) {
                    Node rm = sib;
                    sib = sib.nextSibling();
                    rm.remove();
                    continue;
                }
            }
            break; // 碰到真正有内容就停
        }
    }




    private String generateQrCodeDataUrl(String content) {
        try {
            int size = 256; // 二维码尺寸
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size);

            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", baos);

            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            return "";
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
            System.err.println("HTML导出Word出错！");
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "HTML导出Word失败");
        }
    }

    @Override
    public List<ReadTextbookReturnParam> readTextbook(Long id) {
        if (id == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }

        List<ReadTextbookReturnParam> result = new ArrayList<>();

        // 1. 获取已按sort排好序的原始列表
        LambdaQueryWrapper<TextbookCatalog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookCatalog::getTextbookId, id);
        queryWrapper.orderBy(true, true, TextbookCatalog::getSort);
        List<TextbookCatalog> textbookCatalogList = this.list(queryWrapper);

        if (textbookCatalogList == null || textbookCatalogList.isEmpty()) {
            return new ArrayList<>();
        }

        for (TextbookCatalog textbookCatalog : textbookCatalogList) {
            ReadTextbookReturnParam textbookReturnParam = new ReadTextbookReturnParam();
            BeanUtils.copyProperties(textbookCatalog, textbookReturnParam);
            result.add(textbookReturnParam);
        }

        for (ReadTextbookReturnParam textbookCatalog : result) {
            String rawHtml = textbookCatalog.getCatalogName();
            String plainText = Jsoup.parse(rawHtml).text(); // 去除HTML标签
            textbookCatalog.setCatalogNameWithoutHtml(plainText);
        }

        // 2. 获取需要应用的批注内容
        List<LearningAnnotationsAndLabels> learningAnnotationsAndLabels = labelsService.selectLabels(id);
        if (learningAnnotationsAndLabels == null || learningAnnotationsAndLabels.isEmpty()) {
            return result;
        }

        // 3. 创建一个仅用于快速查找的Map
        Map<Long, TextbookCatalog> lookupMap = result.stream()
                .collect(Collectors.toMap(TextbookCatalog::getId, catalog -> catalog));

        // 4. 遍历批注，通过lookupMap快速找到并更新原始列表中的对象
        for (LearningAnnotationsAndLabels annotationsAndLabel : learningAnnotationsAndLabels) {
            Long catalogIdToUpdate = annotationsAndLabel.getCatalogId();
            TextbookCatalog catalogToUpdate = lookupMap.get(catalogIdToUpdate);

            if (catalogToUpdate != null) {
                catalogToUpdate.setContent(annotationsAndLabel.getContent());
            }
        }

        //返回用户姓名
        Textbook textbook = textbookMapper.selectById(id);
        if(textbook != null){
            SysTbuser sysTbuser = sysUserMapper.selectById(textbook.getCreator());
            if(sysTbuser != null){
                for (ReadTextbookReturnParam readTextbookReturnParam : result) {
                    readTextbookReturnParam.setCreatorName(sysTbuser.getNickname());
                }
            }
        }


        return result;
    }

    @Override
    public List<TextbookTree> getTextbookCatalogTree(Long textbookId) {
        List<TextbookCatalog> catalogList = textbookCatalogMapper.selectList(
                new LambdaQueryWrapper<TextbookCatalog>()
                        .eq(TextbookCatalog::getTextbookId, textbookId)
                        .orderByAsc(TextbookCatalog::getSort)
        );

        Map<Long, TextbookTree> nodeMap = new HashMap<>();
        for (TextbookCatalog record : catalogList) {
            String plainText = null;
            if (record.getCatalogName() != null) {
                plainText = Jsoup.parse(record.getCatalogName()).text();
            }
            TextbookTree node = new TextbookTree()
                    .setCatalogId(record.getId())  // 添加这一行
                    .setTextbookId(record.getTextbookId())
                    .setCatalogName(plainText)
                    .setCatalogLevel(record.getCatalogLevel())
                    .setFatherCatalogId(record.getFatherCatalogId())
                    .setSort(record.getSort())
                    .setChildren(new ArrayList<>());
            nodeMap.put(record.getId(), node);
        }

        List<TextbookTree> roots = new ArrayList<>();
        for (TextbookCatalog record : catalogList) {
            TextbookTree node = nodeMap.get(record.getId());
            if (record.getFatherCatalogId() == null || !nodeMap.containsKey(record.getFatherCatalogId())) {
                roots.add(node);
            } else {
                nodeMap.get(record.getFatherCatalogId()).getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    public List<TextbookCatalog> downloadTextbookCatalog(Long textbookId) {
        if (textbookId == null)
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);

        Textbook textbook = textbookMapper.selectById(textbookId);
        if (textbook == null)
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关教材信息有误");
        if (textbook.getReleaseStatus() != 1 || textbook.getReviewStatus() != 1)
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，教材未发布");

        return this.list(new LambdaQueryWrapper<TextbookCatalog>().eq(TextbookCatalog::getTextbookId, textbookId));
    }

    @Override
    public List<ReadTextbookReturnParam> readTextbookCatalog(Long textbookId, Long catalogId) {
        if (textbookId == null || catalogId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }

        // 1. 收集指定目录及其所有子目录的ID
        Set<Long> allCatalogIds = new HashSet<>();
        allCatalogIds.add(catalogId);
        collectAllChildCatalogIds(textbookId, catalogId, allCatalogIds);

        // 2. 查询指定目录及其所有子目录，按sort字段升序排序
        LambdaQueryWrapper<TextbookCatalog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookCatalog::getTextbookId, textbookId);
        queryWrapper.in(TextbookCatalog::getId, allCatalogIds);
        queryWrapper.orderByAsc(TextbookCatalog::getSort);

        List<TextbookCatalog> textbookCatalogList = this.list(queryWrapper);

        if (textbookCatalogList == null || textbookCatalogList.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 转换为返回参数（仿照readTextbook方法）
        List<ReadTextbookReturnParam> result = new ArrayList<>();
        for (TextbookCatalog textbookCatalog : textbookCatalogList) {
            ReadTextbookReturnParam textbookReturnParam = new ReadTextbookReturnParam();
            BeanUtils.copyProperties(textbookCatalog, textbookReturnParam);
            result.add(textbookReturnParam);
        }

        // 4. 处理目录名称（去除HTML标签）（仿照readTextbook方法）
        for (ReadTextbookReturnParam textbookCatalog : result) {
            String rawHtml = textbookCatalog.getCatalogName();
            if (rawHtml != null) {
                String plainText = Jsoup.parse(rawHtml).text();
                textbookCatalog.setCatalogNameWithoutHtml(plainText);
            }
        }

        // 5. 获取需要应用的批注内容（仿照readTextbook方法）
        List<LearningAnnotationsAndLabels> learningAnnotationsAndLabels = labelsService.selectLabels(textbookId);
        if (learningAnnotationsAndLabels != null && !learningAnnotationsAndLabels.isEmpty()) {
            // 创建一个仅用于快速查找的Map
            Map<Long, TextbookCatalog> lookupMap = result.stream()
                    .collect(Collectors.toMap(TextbookCatalog::getId, catalog -> catalog));

            // 遍历批注，通过lookupMap快速找到并更新原始列表中的对象
            for (LearningAnnotationsAndLabels annotationsAndLabel : learningAnnotationsAndLabels) {
                Long catalogIdToUpdate = annotationsAndLabel.getCatalogId();
                TextbookCatalog catalogToUpdate = lookupMap.get(catalogIdToUpdate);

                if (catalogToUpdate != null) {
                    catalogToUpdate.setContent(annotationsAndLabel.getContent());
                }
            }
        }

        // 6. 返回用户姓名（仿照readTextbook方法）
        Textbook textbook = textbookMapper.selectById(textbookId);
        if (textbook != null) {
            SysTbuser sysTbuser = sysUserMapper.selectById(textbook.getCreator());
            if (sysTbuser != null) {
                for (ReadTextbookReturnParam readTextbookReturnParam : result) {
                    readTextbookReturnParam.setCreatorName(sysTbuser.getNickname());
                }
            }
        }

        return result;
    }
    //递归手机指定目录所有子目录的ID
    private void collectAllChildCatalogIds(Long textbookId, Long parentId, Set<Long> catalogIds) {
        // 查询直接子目录
        List<TextbookCatalog> childCatalogs = this.list(new LambdaQueryWrapper<TextbookCatalog>()
                .eq(TextbookCatalog::getTextbookId, textbookId)
                .eq(TextbookCatalog::getFatherCatalogId, parentId));

        // 递归收集子目录的子目录
        for (TextbookCatalog child : childCatalogs) {
            Long childId = child.getId();
            if (catalogIds.add(childId)) { // 避免重复添加
                collectAllChildCatalogIds(textbookId, childId, catalogIds);
            }
        }
    }


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
        if (currentCatalog != null) {
            resultList.add(currentCatalog);
        }

        return resultList;
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

    /**
     * 提取第一个标题标签（h1-h4）出现之前的所有HTML代码。
     * @param htmlContent 完整的HTML文件内容字符串
     * @return 第一个标题之前的所有HTML代码。如果文件中没有标题，则返回完整的HTML。
     */
    public String extractAllHtmlBeforeFirstHeading(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return "";
        }

        // 1. 定义我们要查找的标题标签
        String[] headingTags = {"<h1", "<h2", "<h3", "<h4"};

        // 2. 寻找第一个出现的标题标签的位置
        int firstHeadingIndex = -1;

        for (String tag : headingTags) {
            int currentIndex = htmlContent.indexOf(tag);
            if (currentIndex != -1) {
                if (firstHeadingIndex == -1 || currentIndex < firstHeadingIndex) {
                    firstHeadingIndex = currentIndex;
                }
            }
        }

        // 3. 根据找到的位置进行截取
        if (firstHeadingIndex != -1) {
            // 如果找到了标题，就从字符串开头截取到该标题标签开始的位置
            return htmlContent.substring(0, firstHeadingIndex);
        } else {
            // 如果整个文档都没有任何标题，则返回完整的HTML内容
            return htmlContent;
        }
    }

    @Override
    public void exportTextbookToPdf(HttpServletResponse response, Long textbookId, String baseUrl) {
        if (ObjectUtils.isEmpty(textbookId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        try {
            Textbook textbook = textbookMapper.selectById(textbookId);
            if (textbook == null) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "未找到对应教材");
            }

            // 查询目录
            LambdaQueryWrapper<TextbookCatalog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TextbookCatalog::getTextbookId, textbookId);
            lambdaQueryWrapper.orderByAsc(TextbookCatalog::getSort);

            List<TextbookCatalog> textbookCatalogs = textbookCatalogMapper.selectList(lambdaQueryWrapper);

            // 构造 HTML 内容
            StringBuilder htmlBuilder = new StringBuilder();
            String h5HeadCode = textbook.getH5HeadCode();
            htmlBuilder.append(h5HeadCode == null ? "" : h5HeadCode);

            // 保持原来的拼接方式
            List<String> htmlFragments = textbookCatalogs.stream()
                    .flatMap(catalog -> Stream.of(catalog.getCatalogName(), catalog.getContent()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            for (String fragment : htmlFragments) {
                htmlBuilder.append(fragment).append("\n");
            }
            htmlBuilder.append("</body></html>");

            // 原始 HTML
            String mergedHtml = htmlBuilder.toString();

            // 处理掉 file-div1 这类"附件块"
            mergedHtml = processFileDivBlocks(mergedHtml, baseUrl);

            // 将 HTML 转为 PDF
            try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
                // 设置 HTML 加载选项，配置资源加载路径
                HtmlLoadOptions loadOptions = new HtmlLoadOptions();
                loadOptions.setEncoding(StandardCharsets.UTF_8);
                loadOptions.setBaseUri(baseUrl); // 设置基础 URI 以正确加载相对路径图片

                // 创建 PDF 保存选项
                PdfSaveOptions pdfOptions = new PdfSaveOptions();

                // 1. 开启图像压缩（通常默认是开启的，但可以通过降采样来显式控制）
                pdfOptions.setDownsampleOptions(new com.aspose.words.DownsampleOptions());
                pdfOptions.getDownsampleOptions().setDownsampleImages(true);
                // 设置分辨率阈值（例如 220 ppi），低于此分辨率不压缩
                pdfOptions.getDownsampleOptions().setResolution(220);

                // 2. 设置 JPEG 质量（0-100），数值越小压缩越高，体积越小
                // 这相当于旧版本的 setImageCompressionLevel
                pdfOptions.setJpegQuality(70);

                // 3. 开启文本压缩（推荐开启以减小体积）
                pdfOptions.setTextCompression(PdfTextCompression.FLATE);

                // 字体兼容性处理（Linux 部署防乱码）- 预留配置代码
                // FontSettings.setFontsFolder("/usr/share/fonts", true);

                com.aspose.words.Document doc = new com.aspose.words.Document(
                        new ByteArrayInputStream(mergedHtml.getBytes(StandardCharsets.UTF_8)), loadOptions);
                
                // 保存为 PDF 格式并应用优化配置
                doc.save(outStream, pdfOptions);

                // 设置响应头为 PDF 格式
                response.setContentType("application/pdf");

                String fileName = textbook.getTextbookName() + ".pdf";
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
            System.err.println("❌ 导出 PDF 出错！");
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "导出 PDF 失败");
        }
    }
}
