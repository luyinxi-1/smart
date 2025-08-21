package com.upc.modular.teacher.dto;

import com.upc.modular.teacher.entity.Teacher;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class updateBatchTeacherParam {
    List<Teacher> teachers;
}
