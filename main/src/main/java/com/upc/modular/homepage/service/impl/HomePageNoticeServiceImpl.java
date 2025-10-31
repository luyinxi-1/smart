package com.upc.modular.homepage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
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
    public HomePageNoticeReturnParam getHomePageNoticeDetails(Long noticeId) {
        return homePageNoticeMapper.getHomePageNoticeDetails(noticeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertTextbookNotice(List<HomePageNoticeClassListParam> homePageNoticeList) {
        for (HomePageNoticeClassListParam homePageNoticeParam : homePageNoticeList) {
            // 1. 插入 HomePageNotice 数据到 home_page_notice 表
            HomePageNotice homePageNotice = new HomePageNotice();
            BeanUtils.copyProperties(homePageNotice, homePageNoticeParam);
            this.save(homePageNotice);
            Long noticeId = homePageNotice.getId();

            // 2. 根据type和scope_type确定通知范围并获取学生ID列表
            Set<Long> studentUserIdSet = new HashSet<>();

            // ########系统通知#########
            if (SYSTEM_NOTICE.equals(homePageNotice.getType())) { // SYSTEM_NOTICE 系统配置
                    // 通知所有学生用户
                    List<Student> allStudents = studentMapper.selectList(new LambdaQueryWrapper<Student>());
                    List<Long> allStudentUserIds = allStudents.stream()
                            .map(Student::getUserId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    studentUserIdSet.addAll(allStudentUserIds);
            //#####教师通知#####
            } else if (TEACHER_NOTICE.equals(homePageNotice.getType())) { // TEACHER_NOTICE 教师配置
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