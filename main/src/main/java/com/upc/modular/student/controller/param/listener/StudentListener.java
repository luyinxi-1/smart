package com.upc.modular.student.controller.param.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.modular.student.controller.param.dto.StudentImportDto;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.service.IStudentService;
import com.upc.utils.AgeQuantifyUtils;
import com.upc.utils.TypeConversionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class StudentListener extends AnalysisEventListener<StudentImportDto> {
    private static final int BATCH_COUNT = 1000;

    private final Map<String, Student> existStudentMap;

    private final IStudentService studentService;

    /**
     * 新增数据列表
     */
    List<Student> studentList = new ArrayList<>(BATCH_COUNT);
    /**
     * 更新数据列表
     */
    List<Student> studentUpdateList = new ArrayList<>(BATCH_COUNT);

    /**
     * 新增数据条数
     */
    @Getter
    private long insertTotal;

    /**
     * 更新数据条数
     */
    @Getter
    private long updateTotal;

    public StudentListener(IStudentService studentService, List<Student> existStudentMap) {
        this.studentService= studentService;
        this.existStudentMap = existStudentMap.stream()
                .filter(user -> StringUtils.isNotBlank(user.getIdcard()))
                .collect(Collectors.toMap(
                        Student::getIdcard,
                        Function.identity(), // value 就是这个 user 本身
                        (oldVal, newVal) -> oldVal // 如果身份证重复，保留第一个
                ));
    }
    @Override
    public void invoke(StudentImportDto studentImportDto, AnalysisContext analysisContext) {
        // 1. 基础数据校验
        String idcard = studentImportDto.getIdcard();
        if (StringUtils.isBlank(idcard) || idcard.length() < 14) {
            log.warn("跳过无效数据，身份证号格式错误：{}", idcard);
            return;
        }
        // 2. 将DTO对象转换为数据库实体对象
        Student student = new Student();
        BeanUtils.copyProperties(studentImportDto, student);

        // 3. 根据身份证号，自动计算并填充生日和性别
        student.setBirthday(AgeQuantifyUtils.getBirthDateFromIdNumber(idcard));
        student.setGender(TypeConversionUtils.sexToString(AgeQuantifyUtils.getGenderFromIdNumber(idcard)));
        String dateBirth = AgeQuantifyUtils.getBirthDateFromIdNumber(studentImportDto.getIdcard());
        String newGender = TypeConversionUtils.sexToString(AgeQuantifyUtils.getGenderFromIdNumber(studentImportDto.getIdcard()));

        student.setBirthday(dateBirth);
        student.setGender(newGender);

        // 用 map 判断是否存在
        Student existTeacher = existStudentMap.get(student.getIdcard());

        if (existTeacher != null) {
            student.setId(existTeacher.getId()); // 设置 ID 用于更新
            studentUpdateList.add(student);
            updateTotal++;
        } else {
            studentList.add(student);
            insertTotal++;
        }

        if (studentList.size() >= BATCH_COUNT || studentUpdateList.size() >= BATCH_COUNT) {
            this.saveDataAsync();
            studentList.clear();
            studentUpdateList.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        this.saveDataAsync();
        if (insertTotal > 3000) {
            log.warn("导入总数为：{}", insertTotal);
        } else {
            log.info("导入总数为：{}", insertTotal);
        }
    }

    private void saveDataAsync() {
        if (CollectionUtils.isNotEmpty(studentList)) {
            studentService.saveBatch(studentList);
        }
        if (CollectionUtils.isNotEmpty(studentUpdateList)){
            studentService.updateBatchById(studentUpdateList);
        }
        log.info("导入成功");
    }

}
