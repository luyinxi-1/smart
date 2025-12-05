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
        Long currentUserId = currentUser.getId();

        // 2. 判断用户身份 (通过 user_id 去 student 表查，查到了就是学生，查不到就是老师/管理员)
        // 也可以根据 UserUtils 中的 role 字段判断，这里沿用之前查表的逻辑
        Student student = studentService.getOne(Wrappers.<Student>lambdaQuery()
                .eq(Student::getUserId, currentUserId));

        boolean isStudent = (student != null);

        // 3. 构建查询条件
        Page<StudentLearningLog> page = new Page<>(param.getCurrent(), param.getSize());
        LambdaQueryWrapper<StudentLearningLog> wrapper = new LambdaQueryWrapper<>();

        // === 公共查询条件 (所有人都支持) ===
        wrapper.eq(param.getTextbookId() != null, StudentLearningLog::getTextbookId, param.getTextbookId())
                .eq(param.getCatalogId() != null, StudentLearningLog::getCatalogId, param.getCatalogId())
                .eq(param.getStatus() != null, StudentLearningLog::getStatus, param.getStatus())
                // 支持标题模糊查询
                .like(StringUtils.isNotBlank(param.getTitle()), StudentLearningLog::getLogTitle, param.getTitle());

        // === 差异化查询条件 ===
        if (isStudent) {
            // 【学生视角】：强制限制只能查自己的
            // 这里建议用 userId 或 studentId 均可，前提是数据一致
            wrapper.eq(StudentLearningLog::getUserId, currentUserId);

            // 学生不需要查 studentName，因为只能看自己的
        } else {
            // 【教师/管理员视角】：可以查所有人

            // 1. 支持按学生姓名模糊查询
            wrapper.like(StringUtils.isNotBlank(param.getStudentName()), StudentLearningLog::getStudentName, param.getStudentName());

            // 2. 支持筛选特定学生的ID (如果前端传了)
            wrapper.eq(param.getStudentId() != null, StudentLearningLog::getStudentId, param.getStudentId());
        }

        // 4. 排序：按操作时间倒序
        wrapper.orderByDesc(StudentLearningLog::getOperationDatetime);

        // 5. 执行查询
        Page<StudentLearningLog> resultPage = this.page(page, wrapper);


        return resultPage;
    }
    @Override
    public Page<StudentLearningLog> getOwnLogs(StudentLearningLogPageSearchParam param) {
        Long currentUserId = UserUtils.get().getId();

        Page<StudentLearningLog> page = new Page<>(param.getCurrent(), param.getSize());
        LambdaQueryWrapper<StudentLearningLog> wrapper = new LambdaQueryWrapper<>();

        // 只能查看自己的日志
        wrapper.eq(StudentLearningLog::getUserId, currentUserId);

        // 添加其他查询条件
        wrapper.eq(param.getTextbookId() != null, StudentLearningLog::getTextbookId, param.getTextbookId())
               .eq(param.getCatalogId() != null, StudentLearningLog::getCatalogId, param.getCatalogId())
               .eq(param.getStatus() != null, StudentLearningLog::getStatus, param.getStatus());

        // 按操作时间倒序排列
        wrapper.orderByDesc(StudentLearningLog::getOperationDatetime);

        return this.page(page, wrapper);
    }

    @Override
    public Page<StudentLearningLog> getStudentLogs(StudentLearningLogPageSearchParam param) {
        Page<StudentLearningLog> page = new Page<>(param.getCurrent(), param.getSize());
        LambdaQueryWrapper<StudentLearningLog> wrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        wrapper.eq(param.getTextbookId() != null, StudentLearningLog::getTextbookId, param.getTextbookId())
               .eq(param.getCatalogId() != null, StudentLearningLog::getCatalogId, param.getCatalogId())
               .eq(param.getStudentId() != null, StudentLearningLog::getStudentId, param.getStudentId())
               .eq(param.getStatus() != null, StudentLearningLog::getStatus, param.getStatus());

        // 按操作时间倒序排列
        wrapper.orderByDesc(StudentLearningLog::getOperationDatetime);

        return this.page(page, wrapper);
    }

/*    @Override
    public Page<StudentLearningLog> getStudentLogs(StudentLearningLogPageSearchParam param) {
        // 1. 组装分页和查询条件
        Page<StudentLearningLog> page = new Page<>(param.getCurrent(), param.getSize());
        LambdaQueryWrapper<StudentLearningLog> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(param.getTextbookId() != null, StudentLearningLog::getTextbookId, param.getTextbookId())
                .eq(param.getCatalogId() != null, StudentLearningLog::getCatalogId, param.getCatalogId())
                .eq(param.getStudentId() != null, StudentLearningLog::getStudentId, param.getStudentId())
                .eq(param.getStatus() != null, StudentLearningLog::getStatus, param.getStatus());

        // 按操作时间倒序排列
        wrapper.orderByDesc(StudentLearningLog::getOperationDatetime);

        // 2. 执行查询
        Page<StudentLearningLog> resultPage = this.page(page, wrapper);
        List<StudentLearningLog> records = resultPage.getRecords();

        // 如果结果为空，直接返回
        if (records == null || records.isEmpty()) {
            return resultPage;
        }

        // 3. 【核心逻辑】筛选需要更新状态的日志 ID
        // 假设：只有状态为 2 (已提交) 的日志才需要变为 3 (已检阅)
        // 如果你的需求是“不管什么状态都要变3”，可以去掉 filter 条件
        List<Long> idsToUpdate = records.stream()
                .filter(log -> log.getStatus() != null && log.getStatus() == 2)
                .map(StudentLearningLog::getId)
                .collect(Collectors.toList());

        // 4. 批量更新数据库状态
        if (!idsToUpdate.isEmpty()) {
            LambdaUpdateWrapper<StudentLearningLog> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(StudentLearningLog::getId, idsToUpdate)
                    .set(StudentLearningLog::getStatus, 3L) // 更新为 3
                    // 可以在这里顺便记录 检阅时间/检阅人 等
                    .set(StudentLearningLog::getOperationDatetime, LocalDateTime.now());

            this.update(updateWrapper);

            // 5. 更新内存中的返回数据（让前端立刻看到状态已变）
            records.forEach(log -> {
                if (idsToUpdate.contains(log.getId())) {
                    log.setStatus(3L);
                }
            });
        }

        return resultPage;
    }*/

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
        if (!currentUserId.equals(log.getStudentId())) {
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