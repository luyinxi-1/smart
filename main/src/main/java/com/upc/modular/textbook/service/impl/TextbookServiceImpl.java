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
import com.upc.modular.group.entity.Group;
import com.upc.modular.group.mapper.GroupMapper;
import com.upc.modular.institution.service.IInstitutionService;
import com.upc.modular.institution.service.impl.InstitutionServiceImpl;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.teachingactivities.entity.DiscussionTopic;
import com.upc.modular.teachingactivities.entity.DiscussionTopicReply;
import com.upc.modular.teachingactivities.mapper.DiscussionTopicMapper;
import com.upc.modular.teachingactivities.mapper.DiscussionTopicReplyMapper;
import com.upc.modular.teachingactivities.param.DiscussionTopicSecondReplyPageReturnParam;
import com.upc.modular.textbook.entity.*;
import com.upc.modular.textbook.mapper.TextbookAuthorityMapper;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.param.TextbookContentSearchResult;
import com.upc.modular.textbook.param.TextbookIntelligentQueryReturnParam;
import com.upc.modular.textbook.param.*;
import com.upc.modular.textbook.service.ITextbookAuthorityService;
import com.upc.modular.textbook.service.ITextbookClassificationService;
import com.upc.modular.textbook.service.ITextbookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.service.ITextbookTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private DiscussionTopicReplyMapper discussionTopicReplyMapper;
    @Autowired
    private DiscussionTopicMapper discussionTopicMapper;
    
    @Autowired
    private ITextbookTemplateService textbookTemplateService;

    @Override
    public List<TextbookIntelligentQueryReturnParam> smartSearch(String query) {
        if (StringUtils.isBlank(query)) {
            return new ArrayList<>(); // 返回空列表
        }

        // 1. 解析关键词
        List<String> keywords = Arrays.stream(query.split("[,，]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (keywords.isEmpty()) {
            return new ArrayList<>();
        }

        // 2.【教材查询】通过关键词分别在教材名、章节名、章节内容中搜索
        List<Textbook> textbooksByName = findAllTextbooksByKeywords(keywords);
        List<Textbook> textbooksByCatalogName = findAllTextbooksByCatalogName(keywords);
        List<Textbook> textbooksByContent = findAllTextbooksByContent(keywords);
        // 3. 合并所有教材，去除重复项
        Set<Long> textbookIds = new HashSet<>();
        List<Textbook> allMatchedTextbooks = new ArrayList<>();
        
        // 添加通过教材名匹配的教材
        for (Textbook textbook : textbooksByName) {
            if (textbookIds.add(textbook.getId())) {
                allMatchedTextbooks.add(textbook);
            }
        }
        
        // 添加通过章节名匹配的教材
        for (Textbook textbook : textbooksByCatalogName) {
            if (textbookIds.add(textbook.getId())) {
                allMatchedTextbooks.add(textbook);
            }
        }
        
        // 添加通过内容匹配的教材
        for (Textbook textbook : textbooksByContent) {
            if (textbookIds.add(textbook.getId())) {
                allMatchedTextbooks.add(textbook);
            }
        }
        
        // 4. 获取当前用户信息
        UserInfoToRedis currentUser = UserUtils.get();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        Integer userType = currentUser != null ? currentUser.getUserType() : null;
        
        // 5. 遍历每个教材，查找匹配的章节和内容，并进行权限检查
        List<TextbookIntelligentQueryReturnParam> results = new ArrayList<>();
        
        for (Textbook textbook : allMatchedTextbooks) {
            Long targetTextbookId = textbook.getId();
            String textbookName = textbook.getTextbookName();
            
            // 权限检查 - 如果不是管理员，需要检查教材权限
            if (userType == null || userType != 0) { // 非管理员需要检查权限
                if (!hasTextbookAccess(targetTextbookId, currentUserId, userType)) {
                    continue; // 没有权限，跳过该教材
                }
            }
            
            // 获取教材作者信息
            String authorName = textbook.getAuthorName();
            
            // 获取教材更新日期
            LocalDateTime updateDate = textbook.getOperationDatetime();
            
            // 【章节查询】在当前教材下搜索章节名
            TextbookCatalog matchedChapter = findChapterByKeywords(keywords, targetTextbookId);
            String chapterName = (matchedChapter != null && StringUtils.isNotBlank(matchedChapter.getCatalogName()))
                    ? stripHtml(matchedChapter.getCatalogName())
                    : null;

            // 【内容查询】在当前教材下搜索内容
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

            // 优先级2/3: 如果在已找到的章节中没找到内容，或根本没找到章节，则在整个教材中搜索内容
            if (matchedContentCatalog == null) {
                // 在整个教材范围内搜索内容
                matchedContentCatalog = findContentByKeywords(keywords, targetTextbookId);
                chapterName = (matchedContentCatalog != null && StringUtils.isNotBlank(matchedContentCatalog.getCatalogName()))
                        ? stripHtml(matchedContentCatalog.getCatalogName())
                        : null;
            }

            content = (matchedContentCatalog != null) ? stripHtml(matchedContentCatalog.getContent()) : null;
            
            // 添加到结果列表
            TextbookIntelligentQueryReturnParam result = new TextbookIntelligentQueryReturnParam();
            result.setTextbookName(textbookName);
            result.setAuthorName(authorName);
            result.setUpdateDate(updateDate);
            result.setChapterName(chapterName);
            result.setContent(content);
            result.setTextbookId(targetTextbookId);
            result.setChapterId(matchedContentCatalog != null ? matchedContentCatalog.getId() : null);
            results.add(result);
        }

        return results;
    }

    /**
     * 检查用户对教材的访问权限
     * @param textbookId 教材ID
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 是否有访问权限
     */
    private boolean hasTextbookAccess(Long textbookId, Long userId, Integer userType) {
        // 检查教材是否设置了权限控制
        MyLambdaQueryWrapper<TextbookAuthority> authorityWrapper = new MyLambdaQueryWrapper<>();
        authorityWrapper.eq(TextbookAuthority::getTextbookId, textbookId);
        long authorityCount = textbookAuthorityMapper.selectCount(authorityWrapper);
        
        // 如果没有设置权限控制，则默认所有人都可以访问
        if (authorityCount == 0) {
            return true;
        }
        
        // 如果是管理员，可以直接访问
        if (userType != null && userType == 0) {
            return true;
        }
        
        // 如果是教师，可以直接访问
        if (userType != null && userType == 2) {
            // 获取教师所在机构
            SysTbuser user = sysUserService.getById(userId);
            if (user == null) {
                return false;
            }

            Long institutionId = user.getInstitutionId();
            if (institutionId == null) {
                return false;
            }

            // 检查权限：
            // 1. authority_type = 1 且 user_id = 教师的 user_id
            // 2. authority_type = 2 且 visible_institute_id = 教师的 institution_id
            MyLambdaQueryWrapper<TextbookAuthority> accessWrapper = new MyLambdaQueryWrapper<>();
            accessWrapper.eq(TextbookAuthority::getTextbookId, textbookId);
            accessWrapper.and(wrapper ->
                    wrapper.eq(TextbookAuthority::getAuthorityType, 1).eq(TextbookAuthority::getUserId, userId)
                            .or()
                            .eq(TextbookAuthority::getAuthorityType, 2).eq(TextbookAuthority::getVisibleInstituteId, institutionId)
            );

            return textbookAuthorityMapper.selectCount(accessWrapper) > 0;
        }
        
        // 如果是学生，需要检查班级权限
        if (userType != null && userType == 1 && userId != null) {
            // 获取学生信息
            Student student = studentMapper.selectOne(new MyLambdaQueryWrapper<Student>().eq(Student::getUserId, userId));
            if (student == null) {
                return false;
            }
            
            Long classId = student.getClassId();
            if (classId == null) {
                return false;
            }
            
            // 获取班级信息
            Group group = groupMapper.selectById(classId);
            if (group == null) {
                return false;
            }
            
            Long institutionId = group.getInstitutionId();
            if (institutionId == null) {
                return false;
            }
            
            // 检查该机构是否有权限访问该教材
            MyLambdaQueryWrapper<TextbookAuthority> accessWrapper = new MyLambdaQueryWrapper<>();
            accessWrapper.eq(TextbookAuthority::getTextbookId, textbookId)
                         .eq(TextbookAuthority::getAuthorityType, 2) // 机构权限类型
                         .eq(TextbookAuthority::getVisibleInstituteId, institutionId);
            
            return textbookAuthorityMapper.selectCount(accessWrapper) > 0;
        }
        
        return false;
    }

    /**
     * 根据关键词列表模糊查询所有匹配的教材
     * 至少有一个关键词匹配即可 (OR逻辑)
     * 只返回已发布的教材 (release_status = 1)
     */
    private List<Textbook> findAllTextbooksByKeywords(List<String> keywords) {
        MyLambdaQueryWrapper<Textbook> wrapper = new MyLambdaQueryWrapper<>();
        // 只查询已发布的教材
        wrapper.eq(Textbook::getReleaseStatus, 1);
        // OR 逻辑 - 至少一个关键词匹配
        wrapper.and(w -> {
            for (int i = 0; i < keywords.size(); i++) {
                if (i > 0) w.or();
                w.like(Textbook::getTextbookName, keywords.get(i));
            }
        });
        return textbookMapper.selectList(wrapper);
    }

    /**
     * 根据关键词列表模糊查询所有匹配的教材（基于章节名）
     * 至少有一个关键词匹配即可 (OR逻辑)
     * 只返回已发布的教材 (release_status = 1)
     * 对于每本教材只取一条匹配记录
     */
    private List<Textbook> findAllTextbooksByCatalogName(List<String> keywords) {
        // 使用自定义SQL直接获取每本教材的一个匹配记录
        List<Long> textbookIds = textbookCatalogMapper.selectDistinctTextbookIdsByCatalogName(keywords);
        
        if (textbookIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询对应的教材信息
        MyLambdaQueryWrapper<Textbook> textbookWrapper = new MyLambdaQueryWrapper<>();
        textbookWrapper.in(Textbook::getId, textbookIds)
                      .eq(Textbook::getReleaseStatus, 1); // 只查询已发布的教材
        
        return textbookMapper.selectList(textbookWrapper);
    }

    /**
     * 根据关键词列表模糊查询所有匹配的教材（基于章节内容）
     * 至少有一个关键词匹配即可 (OR逻辑)
     * 只返回已发布的教材 (release_status = 1)
     * 对于每本教材只取一条匹配记录
     */
    private List<Textbook> findAllTextbooksByContent(List<String> keywords) {
        // 使用自定义SQL直接获取每本教材的一个匹配记录
        List<Long> textbookIds = textbookCatalogMapper.selectDistinctTextbookIdsByContent(keywords);
        
        if (textbookIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询对应的教材信息
        MyLambdaQueryWrapper<Textbook> textbookWrapper = new MyLambdaQueryWrapper<>();
        textbookWrapper.in(Textbook::getId, textbookIds)
                      .eq(Textbook::getReleaseStatus, 1); // 只查询已发布的教材
        
        return textbookMapper.selectList(textbookWrapper);
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
        
        // 保存教材后，初始化默认模板
        if (textbook.getId() != null) {
            textbookTemplateService.initDefaultTemplate(textbook.getId());
        }
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
        Integer userType = UserUtils.get().getUserType();
        
        // 根据用户类型进行不同的权限过滤
        if (userType == 0) {
            // 管理员：查看所有教材
            returnParams.addAll(textbookPageReturnParams);
        } else if (userType == 1) {
            // 学生：查看所有教材（可根据实际需求调整为只查看有查看权限的教材）
            returnParams.addAll(textbookPageReturnParams);
        } else if (userType == 2) {
            // 教师：只查看有编辑权限的教材
            for (TextbookPageReturnParam returnParam : textbookPageReturnParams) {
                if (textbookAuthorityEditJudge(returnParam.getId(), UserUtils.get().getId())) {
                    returnParams.add(returnParam);
                }
            }
        } else {
            // 其他未知类型：不返回任何教材
            // returnParams 保持为空
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

        // 1. 从数据库获取教材的完整信息
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
                    null
            );
        }

        // 4. 资格审查通过后，进行版本比较
        System.out.println("资格审查通过，开始进行版本比较...");

        // ====== 关键改动：先规范化两个版本号 ======
        String serverVersionRaw = serverTextbook.getTextbookVersion();
        String serverVersion = (serverVersionRaw == null || serverVersionRaw.trim().isEmpty())
                ? null
                : serverVersionRaw.trim();

        String clientVersionNorm = (clientVersion == null || clientVersion.trim().isEmpty())
                ? null
                : clientVersion.trim();

        // 4.1 如果服务端和客户端都是 null -> 视为无需更新
        if (serverVersion == null && clientVersionNorm == null) {
            System.out.println("服务器版本和客户端版本均为 NULL，视为无需更新。");
            return new VersionCheckResultDto(
                    textbookId,
                    "MATCH",
                    "服务器与客户端均未设置版本号，视为无需更新。",
                    null  // 不返回 serverVersion
            );
        }

        // 4.2 只有服务端有版本号，客户端为 null -> 建议更新
        if (serverVersion != null && clientVersionNorm == null) {
            System.out.println("客户端版本为 NULL，服务器有版本号，建议更新。 serverVersion = " + serverVersion);
            return new VersionCheckResultDto(
                    textbookId,
                    "MISMATCH",
                    "服务器已设置版本号，客户端未设置，建议更新。",
                    serverVersion
            );
        }

        // 4.3 只有客户端有版本号，服务器为 null -> 给个明确状态（按你业务自行决定语义）
        if (serverVersion == null && clientVersionNorm != null) {
            System.out.println("服务器版本为 NULL，客户端有版本号，无法比较。 clientVersion = " + clientVersionNorm);
            return new VersionCheckResultDto(
                    textbookId,
                    "NO_SERVER_VERSION",
                    "服务器未设置版本号，无法判断是否需要更新。",
                    null
            );
        }

        // 4.4 双方都有版本号，正常比较
        if (serverVersion.equals(clientVersionNorm)) {
            // **情况B：资格审查通过，且版本一致**
            System.out.println("版本号一致。");
            return new VersionCheckResultDto(
                    textbookId,
                    "MATCH",
                    "版本一致，无需更新。",
                    null
            );
        } else {
            // **情况C：资格审查通过，但版本不一致**
            System.out.println("版本号不一致！服务器版本: " + serverVersion + ", 客户端版本: " + clientVersionNorm);
            return new VersionCheckResultDto(
                    textbookId,
                    "MISMATCH",
                    "版本不一致，建议更新。",
                    serverVersion
            );
        }
    }

    /*    @Override
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
    }*/
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
        // 获取当前用户信息
        UserInfoToRedis currentUser = UserUtils.get();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        Integer userType = currentUser != null ? currentUser.getUserType() : null;

        // 先获取所有教材热度数据
        Page<TextbookHotnessDto> resultPage = textbookMapper.selectTextbookHotnessPage(page);
        
        // 对结果进行权限过滤
        List<TextbookHotnessDto> filteredRecords = resultPage.getRecords().stream()
                .filter(dto -> {
                    // 管理员可以直接查看所有教材
                    if (userType != null && userType == 0) {
                        return true;
                    }
                    // 其他用户需要检查权限
                    return hasTextbookAccess(dto.getId(), currentUserId, userType);
                })
                .collect(Collectors.toList());
                
        // 更新分页结果
        resultPage.setRecords(filteredRecords);
        resultPage.setTotal(filteredRecords.size());
        
        return resultPage;
    }

    @Override
    public Page<TextbookCenterPageReturnParam> getTextbookCenter(TextbookCenterPageSearchParam param) {
        Long userId = UserUtils.get().getId();
        Integer activityType = param.getActivityType();
        if (activityType == null || (activityType != 0 && activityType != 1)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "活动类型参数错误");
        }
        Set<Long> textbookIds = new HashSet<>();
        LambdaQueryWrapper<DiscussionTopicReply> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(DiscussionTopicReply::getCreator, userId)
                .or()
                .eq(DiscussionTopicReply::getOperator, userId);

        List<DiscussionTopicReply> userReplies = discussionTopicReplyMapper.selectList(replyWrapper);

        Set<Long> topicIds = userReplies.stream()
                .map(DiscussionTopicReply::getTopicId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!topicIds.isEmpty()) {
            // 查询这些教学活动对应的教材
            LambdaQueryWrapper<DiscussionTopic> topicWrapper = new LambdaQueryWrapper<>();
            topicWrapper.in(DiscussionTopic::getId, topicIds);
            List<DiscussionTopic> topics = discussionTopicMapper.selectList(topicWrapper);

            // 收集教材ID
            topics.stream()
                    .map(DiscussionTopic::getTextbookId)
                    .filter(Objects::nonNull)
                    .forEach(textbookIds::add);
        }
        //交流反馈
        if(activityType == 0){
            LambdaQueryWrapper<DiscussionTopic> userTopicWrapper = new LambdaQueryWrapper<>();
            userTopicWrapper.eq(DiscussionTopic::getCreator, userId)
                    .or()
                    .eq(DiscussionTopic::getOperator, userId)
                    .eq(DiscussionTopic::getIdentityType, 0); // 只查找identity_type为0的数据
            List<DiscussionTopic> userTopics = discussionTopicMapper.selectList(userTopicWrapper);
            userTopics.stream()
                    .map(DiscussionTopic::getTextbookId)
                    .filter(Objects::nonNull)
                    .forEach(textbookIds::add);
        }
        //教学活动
        if(activityType == 1){
            LambdaQueryWrapper<DiscussionTopic> userTopicWrapper = new LambdaQueryWrapper<>();
            userTopicWrapper.eq(DiscussionTopic::getCreator, userId)
                    .or()
                    .eq(DiscussionTopic::getOperator, userId)
                    .eq(DiscussionTopic::getIdentityType, 1); // 只查找identity_type为1的数据
            List<DiscussionTopic> userTopics = discussionTopicMapper.selectList(userTopicWrapper);
            userTopics.stream()
                    .map(DiscussionTopic::getTextbookId)
                    .filter(Objects::nonNull)
                    .forEach(textbookIds::add);
        }


        List<Textbook> resultTextbooks = textbookIds.isEmpty() ?
                Collections.emptyList() : textbookMapper.selectBatchIds(textbookIds);
        
        // 添加教材名称和教材类型筛选条件
        if (StringUtils.isNotBlank(param.getTextbookName())) {
            String textbookName = param.getTextbookName().trim();
            resultTextbooks = resultTextbooks.stream()
                    .filter(textbook -> textbook.getTextbookName() != null && 
                            textbook.getTextbookName().contains(textbookName))
                    .collect(Collectors.toList());
        }
        
        if (param.getTextbookClassification() != null) {
            resultTextbooks = resultTextbooks.stream()
                    .filter(textbook -> Objects.equals(param.getTextbookClassification(), textbook.getClassification()))
                    .collect(Collectors.toList());
        }
        
        Map<Long, Integer> activityCountMap = countUserActivities(userId, activityType);
        // 转换为返回参数
        List<TextbookCenterPageReturnParam> returnList = new ArrayList<>();
        for (Textbook textbook : resultTextbooks) {
            TextbookCenterPageReturnParam returnParam = new TextbookCenterPageReturnParam();
            // 复制Textbook属性到TextbookCenterPageReturnParam
            org.springframework.beans.BeanUtils.copyProperties(textbook, returnParam);
            // 设置活动数量
            returnParam.setActivityCount(activityCountMap.getOrDefault(textbook.getId(), 0));
            returnList.add(returnParam);
        }

        // 分页处理
        long total = returnList.size();
        long current = param.getCurrent();
        long size = param.getSize();
        long fromIndex = (current - 1) * size;

        if (fromIndex >= total) {
            Page<TextbookCenterPageReturnParam> resultPage = new Page<>(current, size, 0);
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }

        long toIndex = Math.min(fromIndex + size, total);
        List<TextbookCenterPageReturnParam> pageRecords = returnList.subList((int)fromIndex, (int)toIndex);

        Page<TextbookCenterPageReturnParam> resultPage = new Page<>(current, size, total);
        resultPage.setRecords(pageRecords);
        return resultPage;

    }

    /**
     * 统计用户在教材中的活动数量
     * @param userId 用户ID
     * @param activityType 活动类型
     * @return 教材ID到活动数量的映射
     */
    private Map<Long, Integer> countUserActivities(Long userId, Integer activityType) {
        Map<Long, Integer> activityCountMap = new HashMap<>();
        
        // 查询用户参与的discussion_topic_reply记录，且type=1（表示活动）
        LambdaQueryWrapper<DiscussionTopicReply> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.and(wrapper -> wrapper.eq(DiscussionTopicReply::getCreator, userId)
                                          .or()
                                          .eq(DiscussionTopicReply::getOperator, userId))
                   .eq(DiscussionTopicReply::getType, 1); // 只统计type=1的记录，表示直接回复教学活动的记录
        
        List<DiscussionTopicReply> userReplies = discussionTopicReplyMapper.selectList(replyWrapper);
        
        // 获取这些回复关联的教学活动
        Set<Long> topicIds = userReplies.stream()
                .map(DiscussionTopicReply::getTopicId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        if (!topicIds.isEmpty()) {
            // 查询这些教学活动对应的教材
            LambdaQueryWrapper<DiscussionTopic> topicWrapper = new LambdaQueryWrapper<>();
            topicWrapper.in(DiscussionTopic::getId, topicIds);
            List<DiscussionTopic> topics = discussionTopicMapper.selectList(topicWrapper);
            
            // 统计每个教材的活动数量
            Map<Long, Long> textbookActivityCount = topics.stream()
                    .map(DiscussionTopic::getTextbookId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(
                            textbookId -> textbookId,
                            Collectors.counting()
                    ));
            
            // 转换为Integer
            textbookActivityCount.forEach((textbookId, count) -> 
                activityCountMap.put(textbookId, count.intValue()));
        }
        
        // 如果activityType为0，还需要统计用户创建的教学活动
        if (activityType == 0) {
            LambdaQueryWrapper<DiscussionTopic> userTopicWrapper = new LambdaQueryWrapper<>();
            userTopicWrapper.and(wrapper -> wrapper.eq(DiscussionTopic::getCreator, userId)
                                                  .or()
                                                  .eq(DiscussionTopic::getOperator, userId))
                   .eq(DiscussionTopic::getIdentityType, 0); // 只查找identity_type为0的数据
    
            List<DiscussionTopic> userTopics = discussionTopicMapper.selectList(userTopicWrapper);
            Map<Long, Long> userTopicCount = userTopics.stream()
                    .map(DiscussionTopic::getTextbookId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(
                            textbookId -> textbookId,
                            Collectors.counting()
                    ));
    
            // 合并统计结果
            userTopicCount.forEach((textbookId, count) -> 
                activityCountMap.merge(textbookId, count.intValue(), Integer::sum));
        }
        
        // 如果activityType为1，还需要统计用户创建的教学活动
        if (activityType == 1) {
            LambdaQueryWrapper<DiscussionTopic> userTopicWrapper = new LambdaQueryWrapper<>();
            userTopicWrapper.and(wrapper -> wrapper.eq(DiscussionTopic::getCreator, userId)
                                                  .or()
                                                  .eq(DiscussionTopic::getOperator, userId))
                   .eq(DiscussionTopic::getIdentityType, 1); // 只查找identity_type为1的数据
    
            List<DiscussionTopic> userTopics = discussionTopicMapper.selectList(userTopicWrapper);
            Map<Long, Long> userTopicCount = userTopics.stream()
                    .map(DiscussionTopic::getTextbookId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(
                            textbookId -> textbookId,
                            Collectors.counting()
                    ));
    
            // 合并统计结果
            userTopicCount.forEach((textbookId, count) -> 
                activityCountMap.merge(textbookId, count.intValue(), Integer::sum));
        }
        
        return activityCountMap;
    }
    
    @Override
    public List<TextbookContentSearchResult> smartSearchInTextbook(Long textbookId, String query) {
        List<TextbookContentSearchResult> results = new ArrayList<>();

        // 【新增】用于去重的 Set，记录已经存在的 fullPath
        Set<String> existingFullPaths = new HashSet<>();

        // 解析关键词
        List<String> keywords = Arrays.stream(query.split("[,，]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (keywords.isEmpty()) {
            return results;
        }

        // 获取教材下的所有章节内容
        MyLambdaQueryWrapper<TextbookCatalog> wrapper = new MyLambdaQueryWrapper<>();
        wrapper.eq(TextbookCatalog::getTextbookId, textbookId)
                .and(w -> w.isNotNull(TextbookCatalog::getContent)
                        .or()
                        .isNotNull(TextbookCatalog::getCatalogName));
        List<TextbookCatalog> catalogs = textbookCatalogMapper.selectList(wrapper);

        // 遍历每个章节
        for (TextbookCatalog catalog : catalogs) {
            String content = catalog.getContent();
            String catalogName = catalog.getCatalogName();

            // 1. 检查章节名称匹配
            boolean catalogNameMatches = false;
            if (StringUtils.isNotBlank(catalogName)) {
                String cleanCatalogName = stripHtml(catalogName);
                catalogNameMatches = keywords.stream()
                        .allMatch(keyword -> cleanCatalogName.contains(keyword));
            }

            // 如果章节名称匹配
            if (catalogNameMatches) {
                String fullPath = buildCatalogFullPath(catalog);

                // 【关键修改】如果路径已存在，直接跳过，不再添加
                if (existingFullPaths.contains(fullPath)) {
                    continue;
                }

                Long levelOneCatalogId = getLevelOneCatalogId(catalog);

                TextbookContentSearchResult result = new TextbookContentSearchResult();
                result.setFullPath(fullPath);
                result.setCatalogId(levelOneCatalogId);
                result.setMatchedContent(null);

                results.add(result);
                existingFullPaths.add(fullPath); // 【关键修改】标记该路径已处理

                continue; // 命中名称后，跳过后续内容检查
            }

            // 2. 检查内容匹配
            if (StringUtils.isNotBlank(content)) {
                String cleanContent = stripHtml(content);
                boolean allKeywordsMatch = keywords.stream()
                        .allMatch(keyword -> cleanContent.contains(keyword));

                if (allKeywordsMatch) {
                    String fullPath = buildCatalogFullPath(catalog);

                    // 【关键修改】再次检查去重（防止虽然名字没匹配，但内容匹配时生成了同样的路径）
                    if (existingFullPaths.contains(fullPath)) {
                        continue;
                    }

                    String matchedContent = extractMatchedContent(content, keywords);
                    Long levelOneCatalogId = getLevelOneCatalogId(catalog);

                    TextbookContentSearchResult result = new TextbookContentSearchResult();
                    result.setFullPath(fullPath);
                    result.setCatalogId(levelOneCatalogId);
                    result.setMatchedContent(matchedContent);

                    results.add(result);
                    existingFullPaths.add(fullPath); // 【关键修改】标记该路径已处理
                }
            }
        }

        return results;
    }
    
    /**
     * 提取包含关键词的文本片段
     * @param content 完整内容
     * @param keywords 关键词列表
     * @return 包含关键词的文本片段
     */
    private String extractMatchedContent(String content, List<String> keywords) {
        // 移除HTML标签
        String cleanContent = stripHtml(content);
        
        // 查找第一个关键词的位置
        int firstKeywordIndex = cleanContent.length();
        for (String keyword : keywords) {
            int index = cleanContent.indexOf(keyword);
            if (index != -1 && index < firstKeywordIndex) {
                firstKeywordIndex = index;
            }
        }
        
        // 提取关键词周围的文本片段（前后各50个字符）
        int start = Math.max(0, firstKeywordIndex - 50);
        int end = Math.min(cleanContent.length(), firstKeywordIndex + 100);
        
        return cleanContent.substring(start, end);
    }
    
    /**
     * 构建章节的完整路径（包含所有父级目录名称）
     * @param catalog 章节
     * @return 完整路径
     */
    private String buildCatalogFullPath(TextbookCatalog catalog) {
        List<String> pathNames = new ArrayList<>();
        
        // 添加当前章节名称
        if (StringUtils.isNotBlank(catalog.getCatalogName())) {
            pathNames.add(stripHtml(catalog.getCatalogName()));
        }
        
        // 向上查找所有父级目录
        Long parentId = catalog.getFatherCatalogId();
        while (parentId != null) {
            TextbookCatalog parentCatalog = textbookCatalogMapper.selectById(parentId);
            if (parentCatalog == null) {
                break;
            }
            
            if (StringUtils.isNotBlank(parentCatalog.getCatalogName())) {
                pathNames.add(0, stripHtml(parentCatalog.getCatalogName())); // 添加到开头
            }
            
            parentId = parentCatalog.getFatherCatalogId();
        }
        
        return String.join(" > ", pathNames);
    }
    
    /**
     * 获取一级目录ID（catalog_level为1的目录）
     * @param catalog 章节
     * @return 一级目录ID
     */
    private Long getLevelOneCatalogId(TextbookCatalog catalog) {
        // 如果当前章节就是一级目录
        if (catalog.getCatalogLevel() != null && catalog.getCatalogLevel() == 1) {
            return catalog.getId();
        }
        
        // 向上查找直到找到一级目录
        Long parentId = catalog.getFatherCatalogId();
        TextbookCatalog current = catalog;
        
        while (parentId != null) {
            TextbookCatalog parentCatalog = textbookCatalogMapper.selectById(parentId);
            if (parentCatalog == null) {
                break;
            }
            
            // 如果找到一级目录
            if (parentCatalog.getCatalogLevel() != null && parentCatalog.getCatalogLevel() == 1) {
                return parentCatalog.getId();
            }
            
            current = parentCatalog;
            parentId = parentCatalog.getFatherCatalogId();
        }
        
        return null;
    }

}
