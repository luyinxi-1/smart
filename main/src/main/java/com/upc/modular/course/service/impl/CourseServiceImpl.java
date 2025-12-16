package com.upc.modular.course.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.course.controller.param.*;
import com.upc.modular.course.entity.Course;
import com.upc.modular.course.entity.CourseClassList;
import com.upc.modular.course.entity.CourseTextbookList;
import com.upc.modular.course.mapper.CourseClassListMapper;
import com.upc.modular.course.mapper.CourseMapper;
import com.upc.modular.course.service.ICourseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.course.service.ICourseTextbookListService;
import com.upc.modular.group.entity.Group;
import com.upc.modular.group.service.IGroupService;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.TextbookMapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.upc.modular.teacher.service.ITeacherService;
import com.upc.modular.group.service.IGroupService;
import com.upc.modular.student.service.IStudentService;
import com.upc.modular.textbook.service.ITextbookService;
import com.upc.modular.course.service.ICourseTextbookListService;
import com.upc.modular.institution.service.IInstitutionService;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.group.entity.Group;
import com.upc.modular.student.entity.Student;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.course.entity.CourseTextbookList;
import com.upc.modular.institution.entity.Institution;
import com.upc.modular.course.entity.CourseClassList;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.upc.modular.course.param.vo.CourseInfoExportVO;
import com.upc.modular.course.export.CourseInfoDocxBuilder;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TextbookMapper textbookMapper;

    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private ITeacherService teacherService;

    @Autowired
    private IGroupService groupService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private ITextbookService textbookService;

    @Autowired
    private ICourseTextbookListService courseTextbookListService;

    @Autowired
    private IInstitutionService institutionService;
    
    @Autowired
    private CourseClassListMapper courseClassListMapper;

    @Override
    public Void deleteCourseByIds(IdParam idParam) {
        List<Long> idList = idParam.getIdList();
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }

        // 查询课程记录
        List<Course> courses = courseMapper.selectBatchIds(idList);
        if (ObjectUtils.isEmpty(courses)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "未找到对应的课程记录");
        }
        this.removeByIds(idList);

        return null;
    }

    @Override
    public Page<CoursePageReturnParam> getPage(CoursePageSearchParam param) {
        // 获取当前用户信息
        Long currentUserId = UserUtils.get().getId();
        Integer userType = UserUtils.get().getUserType();
        
        Page<CoursePageReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        
        // 根据用户类型进行不同的查询
        switch (userType) {
            case 0: // 管理员 - 查看所有课程
                return courseMapper.selectCourse(page, param);
            case 1: // 学生 - 无权访问
                throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "学生无权查看课程列表");
            case 2: // 教师 - 只能查看自己创建的课程
                return courseMapper.selectCourseByTeacher(page, param, currentUserId);
            default:
                throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "无权查看课程列表");
        }
    }

    @Override
    public void exportCourseData(HttpServletResponse response, IdParam param) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");//直接用
        response.setCharacterEncoding("utf-8");//直接用
        try {
            //下面四行直接复制，只需要改"课程信息表"这里
            String fileName = URLEncoder.encode("课程信息表", "UTF-8").
                    replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''"
                    + fileName + ".xlsx");
            // 查询出要返回的
            List<Long> idList = param.getIdList();
            List<Course> courseList = courseMapper.selectList(
                    new LambdaQueryWrapper<Course>()
                            .in(ObjectUtils.isNotNull(idList), Course::getId, idList)
            );
            List<CourseDataExportParam> exportList = courseList.stream()
                    .map(course -> {
                        CourseDataExportParam dto = new CourseDataExportParam();
                        // 复制字段
                        dto.setCourseName(course.getCourseName())
                                .setCredit(course.getCredit())
                                .setDescription(course.getDescription());
                        // status 转换
                        Integer st = course.getStatus();
                        dto.setStatus(st == null ? "未知状态"
                                : st == 0   ? "未发布"
                                : st == 1   ? "已发布"
                                : "其它(" + st + ")");
                        return dto;
                    })
                    .collect(Collectors.toList());
            //TODO
            /**
             * 这里需要等教材表建完之后，把exportList中的教材id变为教材名称。然后把CourseDataExportParam里面的教材id字段改成教材名称。
             */

            EasyExcel.write(response.getOutputStream(), CourseDataExportParam.class)
                    .sheet("课程列表")
                    .doWrite(exportList);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("导出失败，请重试");
        }
    }

    @Override
    public Long inserCourse(Course param) {
        //Long textbookId = param.getTextbookId();
        Long teacherId = param.getTeacherId();


        LambdaQueryWrapper<Teacher> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Teacher::getId, teacherId);
        boolean isTeacherExists = teacherMapper.exists(queryWrapper2);
        if (!isTeacherExists) {
            throw new RuntimeException("ID为 " + teacherId + " 的教师不存在！");
        }
        this.save(param);
        return param.getId();
    }
    
    @Override
    public void exportCourseInfoDocx(HttpServletResponse response, Long courseId, Long classId) {
        try {
            System.out.println("开始导出课程信息文档: courseId=" + courseId + ", classId=" + classId);
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("课程信息导出", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".docx");
            System.out.println("已设置响应头信息");

            // 构建数据
            System.out.println("开始构建课程信息数据");
            CourseInfoExportVO courseInfo = buildCourseInfo(courseId, classId);
            System.out.println("课程信息数据构建完成: courseName=" + courseInfo.getCourseName());

            // 生成文档
            System.out.println("开始生成DOCX文档");
            CourseInfoDocxBuilder builder = new CourseInfoDocxBuilder();
            byte[] documentBytes = builder.buildDocument(courseInfo);
            System.out.println("DOCX文档生成完成，文档大小: " + documentBytes.length + " 字节");

            // 写入响应流
            System.out.println("开始写入响应流");
            response.getOutputStream().write(documentBytes);
            response.getOutputStream().flush();
            System.out.println("响应流写入完成");
        } catch (Exception e) {
            System.err.println("导出课程信息文档失败: courseId=" + courseId + ", classId=" + classId);
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "导出失败: " + e.getMessage());
        }
    }

    private CourseInfoExportVO buildCourseInfo(Long courseId, Long classId) {
        CourseInfoExportVO vo = new CourseInfoExportVO();

        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "课程不存在");
        }
        vo.setCourseName(course.getCourseName());
        vo.setCourseIntro(course.getDescription());

        // 教师
        Long teacherId = course.getTeacherId();
        if (teacherId != null) {
            Teacher teacher = teacherService.getById(teacherId);
            if (teacher != null) {
                vo.setTeacherName(teacher.getName());
                if (teacher.getPhone() != null && !teacher.getPhone().isEmpty()) {
                    vo.setTeacherContact(teacher.getPhone());
                } else if (teacher.getEmail() != null && !teacher.getEmail().isEmpty()) {
                    vo.setTeacherContact(teacher.getEmail());
                }
            }
        }

        // 教材（顺手修 Duplicate key：加 merge 函数；也建议 distinct 后再 listByIds）
        List<CourseTextbookList> courseTextbooks = courseTextbookListService.list(
            new LambdaQueryWrapper<CourseTextbookList>().eq(CourseTextbookList::getCourseId, courseId)
        );

        List<Long> textbookIds = courseTextbooks.stream()
            .map(CourseTextbookList::getTextbookId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        List<Textbook> textbooks = textbookIds.isEmpty() ? new ArrayList<>() : textbookService.listByIds(textbookIds);
        Map<Long, Textbook> textbookMap = textbooks.stream()
            .collect(Collectors.toMap(Textbook::getId, t -> t, (a, b) -> a));

        List<CourseInfoExportVO.TextbookInfo> textbookInfos = new ArrayList<>();
        for (CourseTextbookList ct : courseTextbooks) {
            Textbook textbook = textbookMap.get(ct.getTextbookId());
            if (textbook != null) {
                CourseInfoExportVO.TextbookInfo ti = new CourseInfoExportVO.TextbookInfo();
                ti.setTextbookName(textbook.getTextbookName());
                ti.setAuthorName(textbook.getAuthorName());
                textbookInfos.add(ti);
            }
        }
        vo.setTextbooks(textbookInfos);

        // ====== 多班级核心逻辑 ======
        List<Long> classIds;
        if (classId != null) {
            classIds = Collections.singletonList(classId);
        } else {
            // 从课程-班级关联表查出该课程所有班级
            List<CourseClassList> links = courseClassListMapper.selectList(
                new LambdaQueryWrapper<CourseClassList>().eq(CourseClassList::getCourseId, courseId)
            );
            classIds = links.stream()
                .map(CourseClassList::getClassId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        }

        if (classIds == null || classIds.isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "该课程未关联任何班级");
        }

        // 一次性查出所有学生：按 (classId, 学号) 排序，后面分组时天然就是"先A班后B班"
        List<Student> allStudents = studentService.list(
            new LambdaQueryWrapper<Student>()
                .in(Student::getClassId, classIds)
                .orderByAsc(Student::getClassId)
                .orderByAsc(Student::getIdentityId)
        );
        Map<Long, List<Student>> stuByClass = allStudents.stream()
            .collect(Collectors.groupingBy(Student::getClassId));

        List<CourseInfoExportVO.ClassSection> sections = new ArrayList<>();
        for (Long gid : classIds) {
            Group group = groupService.getById(gid);
            if (group == null) {
                continue; // 或者你想严格点：直接 throw
            }

            CourseInfoExportVO.ClassSection sec = new CourseInfoExportVO.ClassSection();
            sec.setClassId(gid);
            sec.setClassName(group.getName());

            Long institutionId = group.getInstitutionId();
            if (institutionId != null) {
                Institution ins = institutionService.getById(institutionId);
                if (ins != null) {
                    sec.setInstitutionName(ins.getInstitutionName());
                }
            }

            List<Student> stus = stuByClass.getOrDefault(gid, Collections.emptyList());
            List<CourseInfoExportVO.StudentInfo> stuInfos = new ArrayList<>();
            for (Student s : stus) {
                CourseInfoExportVO.StudentInfo si = new CourseInfoExportVO.StudentInfo();
                si.setClassName(sec.getClassName());
                si.setStudentName(s.getName());
                si.setStudentNo(s.getIdentityId());
                si.setPhone(s.getPhone());
                stuInfos.add(si);
            }
            sec.setStudents(stuInfos);

            sections.add(sec);
        }

        vo.setClassSections(sections);

        // 兼容旧字段：如果只导一个班级，让旧字段也有值（避免 builder 旧逻辑空指针）
        if (sections.size() == 1) {
            vo.setClassName(sections.get(0).getClassName());
            vo.setInstitutionName(sections.get(0).getInstitutionName());
            vo.setStudents(sections.get(0).getStudents());
        }

        return vo;
    }
}