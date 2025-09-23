package com.upc.modular.textbook.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "教师批注分页查询参数")
public class TeacherAnnotationPageSearchParam extends PageBaseSearchParam {

    @ApiModelProperty("教材id")
    private Long textbookId;
}
