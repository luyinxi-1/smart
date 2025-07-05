package com.upc.modular.teacher.vo;

import com.upc.modular.teacher.dto.TeacherImportErrorDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ImportTeacherReturnVo {

    @ApiModelProperty(value = "新增数量")
    private long insertTotal;

    @ApiModelProperty(value = "更新数量")
    private long updateTotal;

    @ApiModelProperty(value = "出错条数")
    private long errorTotal;

    @ApiModelProperty(value = "出错详细信息")
    private List<TeacherImportErrorDto> errorDetails;

}
