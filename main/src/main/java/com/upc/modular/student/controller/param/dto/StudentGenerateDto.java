package com.upc.modular.student.controller.param.dto;

import com.upc.modular.student.controller.param.vo.StudentReturnVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
@Data
public class StudentGenerateDto {

    @ApiModelProperty("教师信息")
    private List<StudentReturnVo> student ;

    @ApiModelProperty("机构id")
    private String institutionId;

}
