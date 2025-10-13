package com.upc.modular.materials.controller.param.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class MaterialsTextbookMappingReturnParam {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("教学素材id")
    private Long materialId;

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("章节id")
    private Long chapterId;

    @ApiModelProperty("素材名称")
    private String name;

    @ApiModelProperty("素材类型")
    private String type;

    @ApiModelProperty("素材所在章节")
    private String chapterName;

    @ApiModelProperty("上传人")
    private String uploader;

    @ApiModelProperty("上传时间")
    private LocalDateTime addDatetime;

}
