package com.upc.modular.teacher.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.utils.AgeQuantifyUtils;
import com.upc.utils.TypeConversionUtils;
import com.upc.modular.teacher.dto.TeacherImportDto;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.service.ITeacherService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class TeacherListener extends AnalysisEventListener<TeacherImportDto> {

    private static final int BATCH_COUNT = 1000;

    private final Map<String, Teacher> existTeacherMap;

    private final ITeacherService teacherService;

    /**
     * 新增数据列表
     */
    List<Teacher> teacherList = new ArrayList<>(BATCH_COUNT);
    /**
     * 更新数据列表
     */
    List<Teacher> teacherUpdateList = new ArrayList<>(BATCH_COUNT);

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

    public TeacherListener(ITeacherService teacherService, List<Teacher> existTeacherMap) {
        this.teacherService= teacherService;
        this.existTeacherMap = existTeacherMap.stream()
                .filter(user -> StringUtils.isNotBlank(user.getIdcard()))
                .collect(Collectors.toMap(
                        Teacher::getIdcard,
                        Function.identity(), // value 就是这个 user 本身
                        (oldVal, newVal) -> oldVal // 如果身份证重复，保留第一个
                ));
    }
    @Override
    public void invoke(TeacherImportDto teacherImportDto, AnalysisContext analysisContext) {
        String idcard = teacherImportDto.getIdcard();
        if (StringUtils.isBlank(idcard) || idcard.length() < 14) {
            log.warn("跳过无效数据，身份证号格式错误：{}", idcard);
            return; // 或者记录到错误列表中
        }
        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacherImportDto, teacher);
        String dateBirth = AgeQuantifyUtils.getBirthDateFromIdNumber(teacherImportDto.getIdcard());
        String newGender = TypeConversionUtils.sexToString(AgeQuantifyUtils.getGenderFromIdNumber(teacherImportDto.getIdcard()));

        teacher.setBirthday(dateBirth);
        teacher.setGender(newGender);

        // 用 map 判断是否存在
        Teacher existTeacher = existTeacherMap.get(teacher.getIdcard());

        if (existTeacher != null) {
            teacher.setId(existTeacher.getId()); // 设置 ID 用于更新
            teacherUpdateList.add(teacher);
            updateTotal++;
        } else {
            teacherList.add(teacher);
            insertTotal++;
        }

        if (teacherList.size() >= BATCH_COUNT || teacherUpdateList.size() >= BATCH_COUNT) {
            this.saveDataAsync();
            teacherList.clear();
            teacherUpdateList.clear();
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
        if (CollectionUtils.isNotEmpty(teacherList)) {
            teacherService.saveBatch(teacherList);
        }
        if (CollectionUtils.isNotEmpty(teacherUpdateList)){
            teacherService.updateBatchById(teacherUpdateList);
        }
        log.info("导入成功");
    }
}

