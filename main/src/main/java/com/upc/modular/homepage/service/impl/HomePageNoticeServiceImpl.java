package com.upc.modular.homepage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.homepage.entity.HomePageNotice;
import com.upc.modular.homepage.entity.HomePageNoticeReadStatus;
import com.upc.modular.homepage.mapper.HomePageNoticeMapper;
import com.upc.modular.homepage.mapper.HomePageNoticeReadStatusMapper;
import com.upc.modular.homepage.mapper.StudentCourseTeacherUserViewMapper;
import com.upc.modular.homepage.mapper.TextbookComprehensiveViewMapper;
import com.upc.modular.homepage.param.HomePageNoticeClassListParam;
import com.upc.modular.homepage.param.HomePageNoticeListSearchParam;
import com.upc.modular.homepage.param.HomePageNoticePageSearchParam;
import com.upc.modular.homepage.param.HomePageNoticeReturnParam;
import com.upc.modular.homepage.service.IHomePageNoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-15
 */
@Service
public class HomePageNoticeServiceImpl extends ServiceImpl<HomePageNoticeMapper, HomePageNotice> implements IHomePageNoticeService {

    // 定义常量
    private static final String SYSTEM_NOTICE = "SYSTEM_NOTICE";
    private static final String TEACHER_NOTICE = "TEACHER_NOTICE";

    @Autowired
    private HomePageNoticeMapper homePageNoticeMapper;

    @Autowired
    private HomePageNoticeReadStatusMapper homePageNoticeReadStatusMapper;

    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private TextbookComprehensiveViewMapper textbookComprehensiveViewMapper;
    
    @Autowired
    private StudentCourseTeacherUserViewMapper studentCourseTeacherUserViewMapper;
    
    @Autowired
    private ISysUserService sysTbuserService;

    @Override
    public Boolean insert(HomePageNotice homePageNotice) {
        if (ObjectUtils.isEmpty(homePageNotice)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        if (ObjectUtils.isEmpty(homePageNotice.getIsTop())) {
            homePageNotice.setIsTop(0);
        }
        return this.save(homePageNotice);
    }

    @Override
    public Boolean batchDelete(List<Long> idList) {
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.removeBatchByIds(idList);
    }

    @Override
    public Boolean updateNotice(HomePageNotice homePageNotice) {
        if (ObjectUtils.isEmpty(homePageNotice)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.updateById(homePageNotice);
    }

    @Override
    public List<HomePageNoticeReturnParam> getHomePageNotice(HomePageNoticeListSearchParam param) {
        return homePageNoticeMapper.selectNoticeListWithNames(param);
    }

    @Override
    public Page<HomePageNoticeReturnParam> getHomePageNoticePage(HomePageNoticePageSearchParam param) {
        Page<HomePageNoticeReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        return homePageNoticeMapper.selectNoticePageWithNames(page, param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HomePageNoticeReturnParam getHomePageNoticeDetails(Long noticeId) {
        // 获取当前用户ID
        Long userId = UserUtils.get().getId();
    
        // 获取当前用户信息，判断是否为学生
        SysTbuser currentUser = sysTbuserService.getById(userId);
        Integer userType = currentUser != null ? currentUser.getUserType() : null;
    
        boolean isStudent = userType != null && userType == 1;
    
        // 如果是学生，更新阅读状态为已读
        if (isStudent) {
            // 查询该学生对应该通知的阅读状态记录
            HomePageNoticeReadStatus readStatusRecord = homePageNoticeReadStatusMapper.selectOne(
                    new LambdaQueryWrapper<HomePageNoticeReadStatus>()
                            .eq(HomePageNoticeReadStatus::getNotice_id, noticeId)
                            .eq(HomePageNoticeReadStatus::getUser_id, userId)
            );
            
            // 如果存在记录且状态为未读，则更新为已读
            if (readStatusRecord != null && readStatusRecord.getRead_status() == 0) {
                readStatusRecord.setRead_status(1);
                homePageNoticeReadStatusMapper.updateById(readStatusRecord);
            }
        }
        
        return homePageNoticeMapper.getHomePageNoticeDetails(noticeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertTextbookNotice(List<HomePageNoticeClassListParam> homePageNoticeList) {
        for (HomePageNoticeClassListParam homePageNoticeParam : homePageNoticeList) {
            // 1. 插入 HomePageNotice 数据到 home_page_notice 表
            HomePageNotice homePageNotice = new HomePageNotice();
            BeanUtils.copyProperties(homePageNoticeParam,homePageNotice);
            this.save(homePageNotice);
            Long noticeId = homePageNotice.getId();

            // 2. 根据type和scope_type确定通知范围并获取学生ID列表
            Set<Long> studentUserIdSet = new HashSet<>();

            if (TEACHER_NOTICE.equals(homePageNotice.getType())) { // TEACHER_NOTICE 教师配置
                // 获取当前教师ID（从上下文获取）
                Long teacherUserId = UserUtils.get().getId();

                switch (homePageNotice.getScopeType()) {
                    case 0:
                        // 指定的三个范围（1、2、3）
                        // 范围1：收藏教材学生
                        List<Long> favoriteStudentUserIds = getFavoriteTextbookStudentUserIds(teacherUserId);
                        studentUserIdSet.addAll(favoriteStudentUserIds);
                        
                        // 范围2：课程班级学生
                        List<Long> courseClassStudentUserIds = getCourseClassStudentUserIds(teacherUserId);
                        studentUserIdSet.addAll(courseClassStudentUserIds);
                        
                        // 范围3：指定班级学生
                        if (homePageNoticeParam.getClassList() != null && !homePageNoticeParam.getClassList().isEmpty()) {
                            List<Student> classStudents = studentMapper.selectList(
                                    new LambdaQueryWrapper<Student>()
                                            .in(Student::getClassId, homePageNoticeParam.getClassList())
                            );
                            List<Long> classStudentUserIds = classStudents.stream()
                                    .map(Student::getUserId)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                            studentUserIdSet.addAll(classStudentUserIds);
                        }
                        break;
                    case 1:
                        // 收藏教材学生
                        List<Long> favoriteStudentUserIds1 = getFavoriteTextbookStudentUserIds(teacherUserId);
                        studentUserIdSet.addAll(favoriteStudentUserIds1);
                        break;
                    case 2:
                        // 课程班级学生
                        List<Long> courseClassStudentUserIds2 = getCourseClassStudentUserIds(teacherUserId);
                        studentUserIdSet.addAll(courseClassStudentUserIds2);
                        break;
                    case 3:
                        // 指定班级学生
                        if (homePageNoticeParam.getClassList() != null && !homePageNoticeParam.getClassList().isEmpty()) {
                            List<Student> classStudents = studentMapper.selectList(
                                    new LambdaQueryWrapper<Student>()
                                            .in(Student::getClassId, homePageNoticeParam.getClassList())
                            );
                            List<Long> classStudentUserIds = classStudents.stream()
                                    .map(Student::getUserId)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                            studentUserIdSet.addAll(classStudentUserIds);
                        }
                        break;
                    case 4:
                        // 1和2的组合
                        List<Long> favoriteStudentUserIds4 = getFavoriteTextbookStudentUserIds(teacherUserId);
                        List<Long> courseClassStudentUserIds4 = getCourseClassStudentUserIds(teacherUserId);
                        studentUserIdSet.addAll(favoriteStudentUserIds4);
                        studentUserIdSet.addAll(courseClassStudentUserIds4);
                        break;
                    case 5:
                        // 1和3的组合
                        List<Long> favoriteStudentUserIds5 = getFavoriteTextbookStudentUserIds(teacherUserId);
                        studentUserIdSet.addAll(favoriteStudentUserIds5);
                        
                        if (homePageNoticeParam.getClassList() != null && !homePageNoticeParam.getClassList().isEmpty()) {
                            List<Student> classStudents = studentMapper.selectList(
                                    new LambdaQueryWrapper<Student>()
                                            .in(Student::getClassId, homePageNoticeParam.getClassList())
                            );
                            List<Long> classStudentUserIds = classStudents.stream()
                                    .map(Student::getUserId)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                            studentUserIdSet.addAll(classStudentUserIds);
                        }
                        break;
                    case 6:
                        // 2和3的组合
                        List<Long> courseClassStudentUserIds6 = getCourseClassStudentUserIds(teacherUserId);
                        studentUserIdSet.addAll(courseClassStudentUserIds6);
                        
                        if (homePageNoticeParam.getClassList() != null && !homePageNoticeParam.getClassList().isEmpty()) {
                            List<Student> classStudents = studentMapper.selectList(
                                    new LambdaQueryWrapper<Student>()
                                            .in(Student::getClassId, homePageNoticeParam.getClassList())
                            );
                            List<Long> classStudentUserIds = classStudents.stream()
                                    .map(Student::getUserId)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                            studentUserIdSet.addAll(classStudentUserIds);
                        }
                        break;
                }
            }

            // 3. 批量插入home_page_notice_read_status表记录
            if (!studentUserIdSet.isEmpty()) {
                List<HomePageNoticeReadStatus> readStatusList = studentUserIdSet.stream()
                        .map(userId -> {
                            HomePageNoticeReadStatus readStatus = new HomePageNoticeReadStatus();
                            readStatus.setNotice_id(noticeId);
                            readStatus.setUser_id(userId);
                            readStatus.setRead_status(0); // 默认未读
                            readStatus.setCreate_time(LocalDateTime.now());
                            return readStatus;
                        })
                        .collect(Collectors.toList());

                // 批量保存到数据库
                for (HomePageNoticeReadStatus readStatus : readStatusList) {
                    homePageNoticeReadStatusMapper.insert(readStatus);
                }
            }
        }

        return true;
    }

    @Override
    public Page<HomePageNoticeReturnParam> getHomePageNoticePage2(HomePageNoticePageSearchParam param) {
        // 获取当前用户ID
        Long userId = UserUtils.get().getId();
        
        // 获取当前用户信息，判断用户类型
        SysTbuser currentUser = sysTbuserService.getById(userId);
        Integer userType = currentUser != null ? currentUser.getUserType() : null;
        
        Page<HomePageNoticeReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        Page<HomePageNoticeReturnParam> resultPage;

        // 根据通知类型执行不同的查询逻辑
        if (param.getType() != null && SYSTEM_NOTICE.equals(param.getType())) {
            // 系统通知 - 所有用户都可以查看所有系统通知
            resultPage = homePageNoticeMapper.selectNoticePageWithNames(page, param);
        } else if (param.getType() != null && TEACHER_NOTICE.equals(param.getType())) {
            // 教师通知 - 需要根据用户类型和创建者进行过滤
            if (userType != null && userType == 0) {
                // 管理员 - 可以查看所有教师通知
                resultPage = homePageNoticeMapper.selectNoticePageWithNames(page, param);
            } else if (userType != null && userType == 2) {
                // 教师 - 只查询自己创建的
                resultPage = homePageNoticeMapper.selectNoticePageWithNamesAndCreator(page, param, userId);
            } else {
                // 学生或其他用户类型，使用原始查询逻辑，但后续需要筛选
                resultPage = homePageNoticeMapper.selectNoticePageWithNames(page, param);
            }
        } else {
            // 未指定类型或其他情况，使用原始查询逻辑
            resultPage = homePageNoticeMapper.selectNoticePageWithNames(page, param);
        }
    
        boolean isStudent = userType != null && userType == 1;
    
        // 只有当用户是学生时，才进行特殊处理
        if (isStudent) {
            // 获取通知ID列表
            List<Long> noticeIds = resultPage.getRecords().stream()
                    .map(HomePageNoticeReturnParam::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        
            if (!noticeIds.isEmpty()) {
                // 查询当前用户在这些通知中的阅读状态记录
                List<HomePageNoticeReadStatus> readStatusList = homePageNoticeReadStatusMapper.selectList(
                        new LambdaQueryWrapper<HomePageNoticeReadStatus>()
                                .select(HomePageNoticeReadStatus::getNotice_id, HomePageNoticeReadStatus::getRead_status)
                                .eq(HomePageNoticeReadStatus::getUser_id, userId)
                                .in(HomePageNoticeReadStatus::getNotice_id, noticeIds)
                );
            
                // 构建通知ID到阅读状态的映射
                Map<Long, Integer> noticeReadStatusMap = readStatusList.stream()
                        .collect(Collectors.toMap(
                                HomePageNoticeReadStatus::getNotice_id,
                                HomePageNoticeReadStatus::getRead_status
                        ));
            
                // 筛选出学生有权查看的通知（即在HomePageNoticeReadStatus表中存在记录的通知）
                List<HomePageNoticeReturnParam> filteredNotices = resultPage.getRecords().stream()
                        .filter(notice -> noticeReadStatusMap.containsKey(notice.getId()))
                        .collect(Collectors.toList());
            
                // 更新分页结果
                resultPage.setRecords(filteredNotices);
            
                // 设置每条通知的阅读状态
                resultPage.getRecords().forEach(notice -> {
                    Integer readStatus = noticeReadStatusMap.get(notice.getId());
                    notice.setReadStatus(readStatus != null ? readStatus : 0); // 默认未读
                });
            } else {
                // 如果没有通知，清空记录列表
                resultPage.setRecords(new ArrayList<>());
            }
        } else {
//            // 如果不是学生，保持原有逻辑处理阅读状态
//            // 获取通知ID列表
//            List<Long> noticeIds = resultPage.getRecords().stream()
//                    .map(HomePageNoticeReturnParam::getId)
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//
//            // 查询用户的阅读状态
//            if (!noticeIds.isEmpty()) {
//                // 查询当前用户对这些通知的阅读状态
//                List<HomePageNoticeReadStatus> readStatusList = homePageNoticeReadStatusMapper.selectList(
//                        new LambdaQueryWrapper<HomePageNoticeReadStatus>()
//                                .select(HomePageNoticeReadStatus::getNotice_id, HomePageNoticeReadStatus::getRead_status)
//                                .eq(HomePageNoticeReadStatus::getUser_id, userId)
//                                .in(HomePageNoticeReadStatus::getNotice_id, noticeIds)
//                );
//
//                // 构建通知ID到阅读状态的映射
//                Map<Long, Integer> noticeReadStatusMap = readStatusList.stream()
//                        .collect(Collectors.toMap(
//                                HomePageNoticeReadStatus::getNotice_id,
//                                HomePageNoticeReadStatus::getRead_status
//                        ));
//
//                // 设置每条通知的阅读状态
//                resultPage.getRecords().forEach(notice -> {
//                    Integer readStatus = noticeReadStatusMap.get(notice.getId());
//                    notice.setReadStatus(readStatus != null ? readStatus : 0); // 默认未读
//                });
//            } else {
//                // 如果没有通知，将所有通知的阅读状态设为未读
//                resultPage.getRecords().forEach(notice -> notice.setReadStatus(0));
//            }
        }
        
        return resultPage;
    }

    /**
     * 获取收藏教材的学生用户ID列表
     * @param teacherUserId 教师用户ID
     * @return 学生用户ID列表
     */
    private List<Long> getFavoriteTextbookStudentUserIds(Long teacherUserId) {
        return textbookComprehensiveViewMapper.getStudentUserIdsByTeacherUserId(teacherUserId);
    }

    /**
     * 获取课程班级的学生用户ID列表
     * @param teacherUserId 教师用户ID
     * @return 学生用户ID列表
     */
    private List<Long> getCourseClassStudentUserIds(Long teacherUserId) {
        return studentCourseTeacherUserViewMapper.getStudentUserIdsByTeacherUserId(teacherUserId);
    }
}