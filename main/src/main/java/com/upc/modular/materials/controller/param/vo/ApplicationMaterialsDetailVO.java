package com.upc.modular.materials.controller.param.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 应用素材关联的教学素材详情视图对象
 * </p>
 *
 * @author system
 * @since 2025-10-29
 */
@Data
@ApiModel("应用素材关联的教学素材详情视图对象")
public class ApplicationMaterialsDetailVO {

    @ApiModelProperty("关联ID")
    private Long id;

    @ApiModelProperty("应用素材ID")
    private Long applicationMaterialId;

    @ApiModelProperty("教学素材ID")
    private Long teachingMaterialId;

    @ApiModelProperty("教学素材名称")
    private String teachingMaterialName;

    @ApiModelProperty("素材类型")
    private String type;

    @ApiModelProperty("文件路径")
    private String filePath;

    @ApiModelProperty("封面图片路径")
    private String coverImagePath;

    @ApiModelProperty("文件大小(单位：M)")
    private Double fileSize;

    @ApiModelProperty("排序序号")
    private Integer sequence;
}
