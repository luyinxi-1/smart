package com.upc.modular.textbook.param;

import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.textbook.entity.TextbookAuthority;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TextbookAuthorityDetailReturnParam extends TextbookAuthority {

    @ApiModelProperty("教师信息")
    private Teacher teacher;

    @ApiModelProperty("教师所在组织ID")
    private Long teacherInstitutionId;

    @ApiModelProperty("教师所在组织名称")
    private String teacherInstitutionName;
}
