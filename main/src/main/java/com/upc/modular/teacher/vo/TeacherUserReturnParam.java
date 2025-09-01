package com.upc.modular.teacher.vo;

import com.upc.modular.teacher.entity.Teacher;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TeacherUserReturnParam extends Teacher {
    @ApiModelProperty("上次登录时间")
    private LocalDateTime lastLoginTime;
}
