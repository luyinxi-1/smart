package com.upc.modular.materials.controller.param.dto;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MaterialsTextbookMappingPageSearchParam extends PageBaseSearchParam {

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("模糊查询参数 教学素材名称")
    private String materialName;

    @ApiModelProperty("模糊查询参数 素材所在章节")
    private String chapter;

    @ApiModelProperty("章节id")
    private Long chapterId;

}
