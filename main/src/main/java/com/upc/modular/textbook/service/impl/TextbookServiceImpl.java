package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.auth.service.impl.SysUserServiceImpl;
import com.upc.modular.institution.service.IInstitutionService;
import com.upc.modular.institution.service.impl.InstitutionServiceImpl;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.teachingactivities.param.DiscussionTopicSecondReplyPageReturnParam;
import com.upc.modular.textbook.entity.*;
import com.upc.modular.textbook.mapper.TextbookAuthorityMapper;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.param.*;
import com.upc.modular.textbook.service.ITextbookAuthorityService;
import com.upc.modular.textbook.service.ITextbookClassificationService;
import com.upc.modular.textbook.service.ITextbookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-08
 */
@Service
public class TextbookServiceImpl extends ServiceImpl<TextbookMapper, Textbook> implements ITextbookService {

    @Autowired
    private TextbookMapper textbookMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private TextbookClassificationServiceImpl textbookClassificationService;
    @Autowired
    private TextbookAuthorityMapper textbookAuthorityMapper;
    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private InstitutionServiceImpl institutionService;
    @Autowired
    private TextbookCatalogMapper textbookCatalogMapper;


    @Override
    public TextbookIntelligentQueryReturnParam smartSearch(String query) {
        if (StringUtils.isBlank(query)) {
            return new TextbookIntelligentQueryReturnParam(); // 返回空结果
        }

        // 1. 解析关键词
        List<String> keywords = Arrays.stream(query.split("[,，]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (keywords.isEmpty()) {
            return new TextbookIntelligentQueryReturnParam();
        }

        // 2.【教材查询】优先通过关键词匹配教材
        Textbook matchedTextbook = findTextbookByKeywords(keywords);
        Long targetTextbookId = (matchedTextbook != null) ? matchedTextbook.getId() : null;
        String textbookName = (matchedTextbook != null) ? matchedTextbook.getTextbookName() : null;

        // 3.【章节查询】根据是否匹配到教材，确定章节的搜索范围
        // 优先在已匹配的教材下搜索章节名，否则在所有章节名中搜索
        TextbookCatalog matchedChapter = findChapterByKeywords(keywords, targetTextbookId);
        String chapterName = (matchedChapter != null && StringUtils.isNotBlank(matchedChapter.getCatalogName()))
                ? stripHtml(matchedChapter.getCatalogName())
                : null;

        // 4.【内容查询】根据是否匹配到教材，确定内容的搜索范围
        // 优先在已匹配的教材下搜索内容，优先在已匹配的章节下搜索内容否则在所有内容中搜索
        String content = null;
        TextbookCatalog matchedContentCatalog = null;

        // 优先级1: 如果找到了章节，首先检查该章节的内容是否也匹配关键词
        if (matchedChapter != null && StringUtils.isNotBlank(matchedChapter.getContent())) {
            // 在内存中检查已找到章节的内容是否包含所有关键词
            boolean allKeywordsInContent = keywords.stream()
                    .allMatch(key -> matchedChapter.getContent().contains(key));
            if (allKeywordsInContent) {
                matchedContentCatalog = matchedChapter; // 内容就在已找到的章节里，这是最佳匹配
            }
        }

        // 优先级2/3: 如果在已找到的章节中没找到内容，或根本没找到章节，则进行更广泛的数据库搜索
        if (matchedContentCatalog == null) {
            // 根据是否找到教材，在教材范围内或全局范围内搜索内容
            matchedContentCatalog = findContentByKeywords(keywords, targetTextbookId);
        }

        content = (matchedContentCatalog != null) ? stripHtml(matchedContentCatalog.getContent()) : null;

        // 5. 组装最终结果
        return new TextbookIntelligentQueryReturnParam(textbookName, chapterName, content);
    }

    /**
     * 根据关键词列表模糊查询教材，返回找到的第一个。
     * 所有关键词都必须在教材名称中出现 (AND逻辑)。
     */
    private Textbook findTextbookByKeywords(List<String> keywords) {
        MyLambdaQueryWrapper<Textbook> wrapper = new MyLambdaQueryWrapper<>();
        keywords.forEach(keyword -> wrapper.like(Textbook::getTextbookName, keyword));
        wrapper.last("LIMIT 1"); // 优化查询，只取第一个
        return textbookMapper.selectOne(wrapper);
    }

    /**
     * 根据关键词列表和可选的教材ID，在【章节名称】中模糊查询，返回找到的第一个。
     * 所有关键词都必须在章节名中出现 (AND逻辑)。
     */
    private TextbookCatalog findChapterByKeywords(List<String> keywords, Long textbookId) {
        MyLambdaQueryWrapper<TextbookCatalog> wrapper = new MyLambdaQueryWrapper<>();
        if (textbookId != null) {
            wrapper.eq(TextbookCatalog::getTextbookId, textbookId);
        }
        // AND 逻辑
        keywords.forEach(key -> wrapper.like(TextbookCatalog::getCatalogName, key));
        wrapper.last("LIMIT 1");
        return textbookCatalogMapper.selectOne(wrapper);
    }

    /**
     * 根据关键词列表和可选的教材ID，在【章节内容】中模糊查询，返回找到的第一个。
     * 所有关键词都必须在内容中出现 (AND逻辑)。
     */
    private TextbookCatalog findContentByKeywords(List<String> keywords, Long textbookId) {
        MyLambdaQueryWrapper<TextbookCatalog> wrapper = new MyLambdaQueryWrapper<>();
        if (textbookId != null) {
            wrapper.eq(TextbookCatalog::getTextbookId, textbookId);
        }
        // AND 逻辑
        keywords.forEach(key -> wrapper.like(TextbookCatalog::getContent, key));
        wrapper.last("LIMIT 1");
        return textbookCatalogMapper.selectOne(wrapper);
    }

    /**
     * 工具方法：移除字符串中的HTML标签
     */
    private String stripHtml(String html) {
        if (html == null) return null;
        return html.replaceAll("<[^>]*>", "");
    }



    @Override
    public void insert(Textbook textbook) {
        if (ObjectUtils.isEmpty(textbook)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        if (ObjectUtils.isEmpty(textbook.getReleaseStatus())) {
            textbook.setReleaseStatus(0);
        }
        if (ObjectUtils.isEmpty(textbook.getReviewStatus())) {
            textbook.setReviewStatus(0);
        }
        if (ObjectUtils.isEmpty(textbook.getTextbookAuthorId())) {
            List<Teacher> teachers = teacherMapper.selectList(new MyLambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, UserUtils.get().getId()));
            if (ObjectUtils.isNotEmpty(teachers) && ObjectUtils.isNotEmpty(teachers.get(0).getId())) {
                textbook.setTextbookAuthorId(teachers.get(0).getId());
            }
        }
        this.save(textbook);
    }

    @Override
    public void deleteDictItemByIds(IdParam idParam) {
        if (ObjectUtils.isEmpty(idParam.getIdList())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        textbookMapper.deleteBatchIds(idParam.getIdList());
    }

    @Override
    public void updateTextbook(Textbook textbook) {
        if (ObjectUtils.isEmpty(textbook) || ObjectUtils.isEmpty(textbook.getId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        this.updateById(textbook);
    }

    @Override
    public Page<TextbookPageReturnParam> getPage(TextbookPageSearchParam param) {
        if (ObjectUtils.isEmpty(UserUtils.get()) || ObjectUtils.isEmpty(UserUtils.get().getUserType())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户类型为空");
        }
        List<TextbookPageReturnParam> textbookPageReturnParams = new ArrayList<>();
        if (ObjectUtils.isEmpty(param.getClassificationId())) {
            textbookPageReturnParams = textbookMapper.selectTextbookPage(param, Collections.emptyList(), UserUtils.get().getUserType());
        } else {
            List<Long> classificationIds = textbookClassificationService.selectTextbookClassificationSubtreeIdList(param.getClassificationId());
            textbookPageReturnParams = textbookMapper.selectTextbookPage(param, classificationIds, UserUtils.get().getUserType());
        }
        List<TextbookPageReturnParam> returnParams = new ArrayList<>();
        if (UserUtils.get().getUserType() == 0) {
            returnParams.addAll(textbookPageReturnParams);
        } else {
            for (TextbookPageReturnParam returnParam : textbookPageReturnParams) {
                if (textbookAuthorityEditJudge(returnParam.getId(), UserUtils.get().getId())) {
                    returnParams.add(returnParam);
                }
            }
        }
        for (TextbookPageReturnParam returnParam : returnParams) {
            if (judgeTextbookViewStatus(returnParam)) {
                returnParam.setViewStatus(1);
            } else {
                returnParam.setViewStatus(2);
            }
        }
        long current = Math.max(1, param.getCurrent());
        long size    = Math.max(1, param.getSize());
        int from = (int) ((current - 1) * size);
        if (from >= returnParams.size()) {
            return new Page<>(current, size);   // 越界空页
        }
        int to = Math.min(from + (int) size, returnParams.size());
        List<TextbookPageReturnParam> pageRecords = returnParams.subList(from, to);

        // 组装 Page
        Page<TextbookPageReturnParam> resultPage = new Page<>();
        resultPage.setCurrent(current);
        resultPage.setSize(size);
        resultPage.setTotal(returnParams.size());
        resultPage.setRecords(pageRecords);
        return resultPage;


    }

    @Override
    public Page<Textbook> queryTextbooksByConditions(TextbookQueryReq req) {
        Page<Textbook> page = new Page<>(req.getPageNum(), req.getPageSize());
        return textbookMapper.queryByConditions(page, req);
    }



    @Override
    public List<Textbook> getNewTextbook(int getNumber) {
        MyLambdaQueryWrapper<Textbook> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(Textbook::getAddDatetime)
                .eq(Textbook::getReleaseStatus, 1)
                .eq(Textbook::getReviewStatus, 1)
                .last("LIMIT " + getNumber);
        return textbookMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public TextbookPageReturnParam getOneTextbookDetails(Long textbookId) {
        if (ObjectUtils.isEmpty(UserUtils.get()) || ObjectUtils.isEmpty(UserUtils.get().getUserType())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户类型为空");
        }
        return textbookMapper.getOneTextbookDetails(textbookId, UserUtils.get().getUserType());
    }


    public boolean textbookAuthorityJudge(Long textBookId, Long userId) {
        if (textBookId == null || userId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        SysTbuser tbuser = sysUserService.getById(userId);
        if (tbuser == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关用户信息有误");
        }
        Long userInstitutionId = tbuser.getInstitutionId();

        Textbook textbook = textbookMapper.selectById(textBookId);
        if (textbook == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关教材信息有误");
        }

        LambdaQueryWrapper<TextbookAuthority> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookAuthority::getTextbookId, textbook.getId());
        queryWrapper.eq(TextbookAuthority::getAuthorityType, 2);
        List<TextbookAuthority> textbookAuthorities = textbookAuthorityMapper.selectList(queryWrapper);
        if (textbookAuthorities.isEmpty()) {
            return true;
        }
        for (TextbookAuthority textbookAuthority : textbookAuthorities) {
            Long visibleInstituteId = textbookAuthority.getVisibleInstituteId();
            boolean result = institutionService.judgeInclusion(userInstitutionId, visibleInstituteId);

            if (result) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Page<Textbook> getpageTextbookCenter(UserFavoritesPageSearch param) {
        UserInfoToRedis userInfoToRedis = UserUtils.get();
        if (ObjectUtils.isEmpty(userInfoToRedis)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户未登录");
        }
        Long currentUserId = userInfoToRedis.getId();
        QueryWrapper<Textbook> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("release_status", 1);
        List<Textbook> allTextbooks = this.list(queryWrapper); // 从数据库加载所有教材到内存

        List<Textbook> authorizedTextbooks = new ArrayList<>();
        for (Textbook textbook : allTextbooks) {
            // 调用您指定的权限判断接口

            boolean hasAuthority = textbookAuthorityJudge(textbook.getId(), currentUserId);
            // 作者本人默认拥有权限
            boolean isAuthor = currentUserId.equals(textbook.getTextbookAuthorId());

            if (hasAuthority || isAuthor) {
                authorizedTextbooks.add(textbook);
            }
        }
        List<Textbook> filteredTextbooks;
        final Long classification = param.getClassification();
        final String textbookName = param.getTextbookName();
        final String authorName = param.getAuthorName();

        final boolean isClassificationEmpty = (classification == null);
        final boolean isTextbookNameEmpty = (textbookName == null || textbookName.trim().isEmpty());
        final boolean isAuthorNameEmpty = (authorName == null || authorName.trim().isEmpty());
        filteredTextbooks = authorizedTextbooks.stream()
                .filter(textbook -> {
                    // 情况一：所有查询条件都为空，不过滤，全部返回
                    if (isClassificationEmpty && isTextbookNameEmpty && isAuthorNameEmpty) {
                        return true;
                    }
                    // 情况二：只有分类不为空
                    if (!isClassificationEmpty && isTextbookNameEmpty && isAuthorNameEmpty) {
                        return classification.equals(textbook.getClassification());
                    }
                    // 情况三：只有教材名称不为空
                    if (isClassificationEmpty && !isTextbookNameEmpty && isAuthorNameEmpty) {
                        // contains 实现模糊查询
                        return textbook.getTextbookName() != null && textbook.getTextbookName().contains(textbookName.trim());
                    }
                    // 情况四：只有作者姓名不为空
                    if (isClassificationEmpty && isTextbookNameEmpty && !isAuthorNameEmpty) {
                        // contains 实现模糊查询
                        return textbook.getAuthorName() != null && textbook.getAuthorName().contains(authorName.trim());
                    }
                    // 情况五：分类和教材名称不为空
                    if (!isClassificationEmpty && !isTextbookNameEmpty && isAuthorNameEmpty) {
                        boolean classificationMatch = classification.equals(textbook.getClassification());
                        boolean nameMatch = textbook.getTextbookName() != null && textbook.getTextbookName().contains(textbookName.trim());
                        return classificationMatch && nameMatch;
                    }
                    // 情况六：分类和作者姓名不为空
                    if (!isClassificationEmpty && isTextbookNameEmpty && !isAuthorNameEmpty) {
                        boolean classificationMatch = classification.equals(textbook.getClassification());
                        boolean authorMatch = textbook.getAuthorName() != null && textbook.getAuthorName().contains(authorName.trim());
                        return classificationMatch && authorMatch;
                    }
                    // 情况七：教材名称和作者姓名不为空
                    if (isClassificationEmpty && !isTextbookNameEmpty && !isAuthorNameEmpty) {
                        boolean nameMatch = textbook.getTextbookName() != null && textbook.getTextbookName().contains(textbookName.trim());
                        boolean authorMatch = textbook.getAuthorName() != null && textbook.getAuthorName().contains(authorName.trim());
                        return nameMatch && authorMatch;
                    }
                    // 情况八：所有条件都不为空
                    if (!isClassificationEmpty && !isTextbookNameEmpty && !isAuthorNameEmpty) {
                        boolean classificationMatch = classification.equals(textbook.getClassification());
                        boolean nameMatch = textbook.getTextbookName() != null && textbook.getTextbookName().contains(textbookName.trim());
                        boolean authorMatch = textbook.getAuthorName() != null && textbook.getAuthorName().contains(authorName.trim());
                        return classificationMatch && nameMatch && authorMatch;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        if (param.getIsAsc() != null && param.getIsAsc() == 1) {
            filteredTextbooks.sort(Comparator.comparing(Textbook::getAddDatetime)); // 升序
        } else {
            filteredTextbooks.sort(Comparator.comparing(Textbook::getAddDatetime).reversed()); // 降序
        }
        long total = filteredTextbooks.size();
        long current = param.getCurrent();
        long size = param.getSize();

        long fromIndex = (current - 1) * size;
        if (fromIndex >= total) {
            Page<Textbook> resultPage = new Page<>(current, size, 0);
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }
        long toIndex = Math.min(fromIndex + size, total);

        List<Textbook> pageRecords = filteredTextbooks.subList((int)fromIndex, (int)toIndex);

        // ==================== 步骤 6: 封装并返回Page对象 ====================
        Page<Textbook> resultPage = new Page<>(current, size, total);
        resultPage.setRecords(pageRecords);

        return resultPage;
    }

    @Override
    public VersionCheckResultDto checkStatusAndVersion(Long textbookId, String clientVersion) {
        System.out.println("Service层收到版本校验请求 - 教材ID: " + textbookId);

        // 1. 从数据库获取教材的完整信息 (可以直接调用 IService 提供的方法)
        Textbook serverTextbook = this.getById(textbookId);

        // 2. 检查教材是否存在
        if (serverTextbook == null) {

            throw new BusinessException(BusinessErrorEnum.NO_EXIT, "服务器未找到ID为 " + textbookId + " 的教材");
        }

        Integer releaseStatus = serverTextbook.getReleaseStatus();
        Integer reviewStatus = serverTextbook.getReviewStatus();

        System.out.println("资格审查 - 发布状态: " + releaseStatus + ", 审查状态: " + reviewStatus);

        // 3. 判断是否满足前置条件 (我们假设状态为 1 代表 "已发布" 和 "已审查")
        boolean isAvailable = (releaseStatus != null && releaseStatus.equals(1)) &&
                (reviewStatus != null && reviewStatus.equals(1));

        if (!isAvailable) {
            // **情况A：资格审查不通过**
            System.out.println("资格审查不通过，教材当前不可用。");
            return new VersionCheckResultDto(
                    textbookId,
                    "UNAVAILABLE",
                    "该教材当前未发布或未通过审查，无法进行版本比较。",
                    null // serverVersion 为 null
            );
        }
        // 4. **只有在资格审查通过后，才执行原有的版本比较逻辑**
        System.out.println("资格审查通过，开始进行版本比较...");
        String serverVersion = serverTextbook.getTextbookVersion();

        if (serverVersion.equals(clientVersion)) {
            // **情况B：资格审查通过，且版本一致**
            System.out.println("版本号一致。");
            return new VersionCheckResultDto(
                    textbookId,
                    "MATCH",
                    "版本一致，无需更新。",
                    null // serverVersion 为 null
            );
        } else {
            // **情况C：资格审查通过，但版本不一致**
            System.out.println("版本号不一致！服务器版本: " + serverVersion + ", 客户端版本: " + clientVersion);
            return new VersionCheckResultDto(
                    textbookId,
                    "MISMATCH",
                    "版本不一致，建议更新。",
                    serverVersion // 附带服务器的最新版本号
            );
        }
    }
    @Override
    public Textbook downloadTextbookInfo(Long textbookId) {

        if (textbookId == null)
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);

        Textbook textbook = textbookMapper.selectById(textbookId);
        // ++++++++++++++++++++++++ 添加这些调试日志 ++++++++++++++++++++++++
        System.out.println("====== 下载接口内部状态调试 ======");
        System.out.println("从数据库获取到的教材ID: " + textbook.getId());
        System.out.println("获取到的 release_status 的值: " + textbook.getReleaseStatus());


        if (textbook.getReleaseStatus() != null) {
            System.out.println("获取到的 release_status 的数据类型: " + textbook.getReleaseStatus().getClass().getName());
        }

        System.out.println("===============================");

        if (textbook == null)
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关教材信息有误");

        if (textbook.getReleaseStatus() != 1 || textbook.getReviewStatus() != 1)
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，教材未发布");
        return textbook;
    }

    public boolean textbookAuthorityEditJudge(Long textBookId, Long userId) {
        if (textBookId == null || userId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        SysTbuser tbuser = sysUserService.getById(userId);
        if (tbuser == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关用户信息有误");
        }
        Textbook textbook = textbookMapper.selectOne(new MyLambdaQueryWrapper<Textbook>().eq(Textbook::getId, textBookId));
        if (textbook == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关教材信息有误");
        }

        Teacher teacher = teacherMapper.selectOne(new MyLambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, userId));
        if (teacher == null || ObjectUtils.isEmpty(teacher.getId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关教师信息有误");
        }

        LambdaQueryWrapper<TextbookAuthority> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookAuthority::getTextbookId, textbook.getId());
        queryWrapper.eq(TextbookAuthority::getAuthorityType, 1);
        List<TextbookAuthority> textbookAuthorities = textbookAuthorityMapper.selectList(queryWrapper);
        if (Objects.equals(textbook.getTextbookAuthorId(), teacher.getId())) {
            // 作者本人
            return true;
        }
        if (Objects.equals(textbook.getCreator(), userId)) {
            return true;
        }
        if (textbookAuthorities.isEmpty()) {
            return false;
        }
        for (TextbookAuthority textbookAuthority : textbookAuthorities) {
            if (Objects.equals(textbookAuthority.getUserId(), userId)) {
                return true;
            }
        }

        return false;
    }

    public boolean judgeTextbookViewStatus(TextbookPageReturnParam returnParam) {
        LambdaQueryWrapper<TextbookAuthority> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookAuthority::getTextbookId, returnParam.getId());
        queryWrapper.eq(TextbookAuthority::getAuthorityType, 2);
        List<TextbookAuthority> textbookAuthorities = textbookAuthorityMapper.selectList(queryWrapper);
        return textbookAuthorities.isEmpty();
    }

    @Override
    public Page<TextbookHotnessDto> getTextbookHotnessPage(Page<TextbookHotnessDto> page) {
        return textbookMapper.selectTextbookHotnessPage(page);
    }
}
