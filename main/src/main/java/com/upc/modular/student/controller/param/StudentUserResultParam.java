package com.upc.modular.student.controller.param;

import com.upc.modular.student.entity.Student;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class StudentUserResultParam extends Student {
    @ApiModelProperty("上次登录时间")
    private LocalDateTime lastLoginTime;
}
