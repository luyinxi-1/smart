package com.upc.modular.materials.controller.param.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 应用素材教材绑定返回参数
 * </p>
 *
 * @author system
 * @since 2025-10-31
 */
@Data
public class ApplicationMaterialsTextbookMappingReturnParam {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("应用素材id")
    private Long applicationMaterialId;

    @ApiModelProperty("应用素材名称")
    private String applicationMaterialName;

    @ApiModelProperty("应用素材类型")
    private String applicationType;

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("素材所在章名")
    private String chapterName;

    @ApiModelProperty("素材所在章id")
    private Long chapterId;

    @ApiModelProperty("教材中应用素材在线浏览次数")
    private Long viewCount;

    @ApiModelProperty("教材中应用素材下载次数")
    private Long downloadCount;

    @ApiModelProperty("创建者")
    private Long creator;

    @ApiModelProperty("创建者名称")
    private String creatorName;

    @ApiModelProperty("创建时间")
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作者")
    private Long operator;

    @ApiModelProperty("操作时间")
    private LocalDateTime operationDatetime;
}

