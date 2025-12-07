package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.utils.MyBeanUtils;
import com.upc.common.utils.UserUtils;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.service.IStudentService;
import com.upc.modular.textbook.controller.param.dto.StudentLearningLogSaveParam;
import com.upc.modular.textbook.entity.StudentLearningLog;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.mapper.StudentLearningLogMapper;
import com.upc.modular.textbook.param.StudentLearningLogPageSearchParam;
import com.upc.modular.textbook.service.IStudentLearningLogService;
import com.upc.modular.textbook.service.ITextbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentLearningLogServiceImpl extends ServiceImpl<StudentLearningLogMapper, StudentLearningLog> implements IStudentLearningLogService {

    @Autowired
    private ITextbookService textbookService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private IStudentService studentService;


    @Override
    public R<Void> saveLog(StudentLearningLogSaveParam param) {
        // 1. 获取当前登录用户信息
        UserInfoToRedis currentUser = UserUtils.get();
        if (currentUser == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "无法获取当前用户信息");
        }

        // 2. 通过 user_id 查询 student 表（获取真实的 student_id 和 student_name）
        Student student = studentService.getOne(Wrappers.<Student>lambdaQuery()
                .eq(Student::getUserId, currentUser.getId())); // 使用登录用户的ID查询

        if (student == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "当前登录账号未绑定学生档案");
        }

        StudentLearningLog log = new StudentLearningLog();
        boolean isNew = (param.getId() == null); // 判断是否为新增

        if (!isNew) {
            // === 更新时的校验逻辑 ===
            log = this.getById(param.getId());
            if (log == null) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "学习日志不存在");
            }
            // 校验权限：判断日志的 student_id 是否等于当前学生的 id
            if (!student.getId().equals(log.getStudentId())) {
                throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "只能操作自己的学习日志");
            }
            // 校验状态
            if (log.getStatus() != null && log.getStatus() == 2) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "已提交的日志不能修改");
            }
        }

        // 3. 属性复制 (必须在手动set之前)
        MyBeanUtils.copy(param, log);

        // 4. 强制设置关联信息 (覆盖前端可能传入的错误数据)
        log.setStudentId(student.getId());     // 存入 student 表的主键
        log.setStudentName(student.getName()); // 存入 student 表的姓名
        log.setOperationDatetime(LocalDateTime.now());

        // 5. 新增时的特殊处理
        if (isNew) {
            // 【新增需求】：保存当前登录用户的 user_id
            log.setUserId(currentUser.getId());

            log.setAddDatetime(LocalDateTime.now());
            log.setStatus(1L); // 默认为1，放在copy之后确保不为NULL
        }

        // 6. 设置教材名称
        if (param.getTextbookId() != null) {
            Textbook textbook = textbookService.getById(param.getTextbookId());
            if (textbook != null) {
                log.setTextbookName(textbook.getTextbookName());
            }
        }

        this.saveOrUpdate(log);
        return R.ok();
    }
    @Override
    public R<Void> submitLog(Long logId) {
        Long currentUserId = UserUtils.get().getId();

        StudentLearningLog log = this.getById(logId);
        if (log == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "学习日志不存在");
        }

        // 权限校验：只能操作自己的日志
        if (!currentUserId.equals(log.getUserId())) {
            throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "只能提交自己的学习日志");
        }

        // 状态校验：只能提交未提交的日志
        if (log.getStatus() != null && log.getStatus() == 2) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "日志已经提交");
        }

        // 更新状态为已提交
        log.setStatus(2L);
        log.setSubmitDatetime(LocalDateTime.now());
        log.setOperationDatetime(LocalDateTime.now());
        this.updateById(log);

        return R.ok();
    }

    @Override
    public Page<StudentLearningLog> getLogPage(StudentLearningLogPageSearchParam param) {
        // 1. 获取当前登录用户
        UserInfoToRedis currentUser = UserUtils.get();
        if (currentUser == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "未获取到登录信息");
        }

        Long currentUserId = currentUser.getId();
        Integer userType = currentUser.getUserType(); // 获取用户类型：0管理员，1学生，2教师

        // 2. 初始化分页和查询包装器
        Page<StudentLearningLog> page = new Page<>(param.getCurrent(), param.getSize());
        LambdaQueryWrapper<StudentLearningLog> wrapper = new LambdaQueryWrapper<>();

        // 3. === 核心权限控制逻辑 ===

        if (userType == 1) {

            wrapper.eq(StudentLearningLog::getUserId, currentUserId);


        } else {


            // 1. 支持按“学生姓名”模糊搜索
            wrapper.like(StringUtils.isNotBlank(param.getStudentName()),
                    StudentLearningLog::getStudentName, param.getStudentName());

            // 2. 支持按“学生ID”精确筛选
            wrapper.eq(param.getStudentId() != null,
                    StudentLearningLog::getStudentId, param.getStudentId());

            if (userType == 2) {

                wrapper.inSql(StudentLearningLog::getTextbookId,
                        "SELECT id FROM textbook WHERE creator = " + currentUserId);
            }

            // 【场景 B-2：管理员 (userType == 0)】
            // 没有任何额外限制，可以查看所有教材、所有学生的日志
        }

        wrapper.eq(param.getTextbookId() != null, StudentLearningLog::getTextbookId, param.getTextbookId())
                .eq(param.getCatalogId() != null, StudentLearningLog::getCatalogId, param.getCatalogId())
                .eq(param.getStatus() != null, StudentLearningLog::getStatus, param.getStatus())
                .like(StringUtils.isNotBlank(param.getTitle()), StudentLearningLog::getLogTitle, param.getTitle());

        // 5. 排序：按操作时间倒序
        wrapper.orderByDesc(StudentLearningLog::getOperationDatetime);

        return this.page(page, wrapper);
    }

    @Override
    public StudentLearningLog getLogDetail(Long logId) {
        StudentLearningLog log = this.getById(logId);
        if (log == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "学习日志不存在");
        }

        Long currentUserId = UserUtils.get().getId();
        Integer currentUserType = UserUtils.get().getUserType();
        
        // 权限校验和状态更新逻辑
        if (currentUserType != null && currentUserType == 2) { // 教师查看日志
            // 如果日志状态为2(已提交)，则更新为3(已查看)
            if (log.getStatus() != null && log.getStatus() == 2L) {
                log.setStatus(3L);
                log.setOperationDatetime(LocalDateTime.now());
                this.updateById(log);
            }
            return log;
        } else if (currentUserType != null && currentUserType == 0) { // 管理员查看日志
            // 管理员可以查看但不更改状态
            return log;
        } else { // 学生查看日志
            if (!currentUserId.equals(log.getUserId())) {
                throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "只能查看自己的学习日志");
            }
            return log;
        }
    }

    @Override
    public R<Void> deleteUnsubmittedLog(Long logId) {
        Long currentUserId = UserUtils.get().getId();

        StudentLearningLog log = this.getById(logId);
        if (log == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "学习日志不存在");
        }

        // 权限校验：只能操作自己的日志
        if (!currentUserId.equals(log.getUserId())) {
            throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "只能删除自己的学习日志");
        }

        // 状态校验：只能删除未提交的日志
        if (log.getStatus() != null && log.getStatus() == 2) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "已提交的日志不能删除");
        }

        this.removeById(logId);
        return R.ok();
    }
}