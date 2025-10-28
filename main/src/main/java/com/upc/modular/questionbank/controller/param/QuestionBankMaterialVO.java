package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 题库素材返回视图对象
 * </p>
 *
 * @author cyy
 * @since 2025-10-27
 */
@Data
@ApiModel("题库素材返回视图对象")
public class QuestionBankMaterialVO {

    @ApiModelProperty("关联ID")
    private Long id;

    @ApiModelProperty("题库ID")
    private Long questionBankId;

    @ApiModelProperty("素材ID")
    private Long materialId;

    @ApiModelProperty("素材名称")
    private String materialName;

    @ApiModelProperty("素材类型")
    private String materialType;

    @ApiModelProperty("文件路径")
    private String filePath;

    @ApiModelProperty("封面图片路径")
    private String coverImagePath;

    @ApiModelProperty("文件大小(单位：M)")
    private Double fileSize;

    @ApiModelProperty("是否公开")
    private Boolean isPublic;

    @ApiModelProperty("排序序号")
    private Integer sequence;

    @ApiModelProperty("创建时间")
    private LocalDateTime addDatetime;
}

