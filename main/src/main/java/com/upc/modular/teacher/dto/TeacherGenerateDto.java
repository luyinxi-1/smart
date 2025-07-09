package com.upc.modular.teacher.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.upc.modular.teacher.vo.TeacherReturnVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TeacherGenerateDto {

    @ApiModelProperty("教师信息")
    private List<TeacherReturnVo> teacher ;

    @ApiModelProperty("机构id")
    private Long institutionId;
}
