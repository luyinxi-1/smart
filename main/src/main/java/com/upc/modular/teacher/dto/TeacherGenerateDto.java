package com.upc.modular.teacher.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.upc.modular.teacher.vo.TeacherReturnVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class TeacherGenerateDto {

    @ApiModelProperty("教师信息")
    private List<TeacherReturnVo> teacher ;

    @ApiModelProperty("机构id")
    private String institutionId;
}
