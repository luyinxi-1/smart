package com.upc.modular.materials.controller.param.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 应用素材教材绑定分页查询参数
 * </p>
 *
 * @author system
 * @since 2025-10-31
 */
@Data
public class ApplicationMaterialsTextbookMappingPageSearchParam {

    @ApiModelProperty("当前页")
    private Integer current = 1;

    @ApiModelProperty("每页大小")
    private Integer size = 10;

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("应用素材id")
    private Long applicationMaterialId;

    @ApiModelProperty("章节名称")
    private String chapterName;

    @ApiModelProperty("章节id")
    private Long chapterId;
}

