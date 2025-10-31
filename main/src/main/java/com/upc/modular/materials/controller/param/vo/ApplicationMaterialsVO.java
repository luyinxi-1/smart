package com.upc.modular.materials.controller.param.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 应用素材返回视图对象
 * </p>
 *
 * @author system
 * @since 2025-10-29
 */
@Data
@ApiModel("应用素材返回视图对象")
public class ApplicationMaterialsVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("应用素材名称")
    private String name;

    @ApiModelProperty("应用素材描述")
    private String description;

    @ApiModelProperty("题库ID")
    private Long questionBankId;
    
    @ApiModelProperty("题库名称")
    private String questionBankName;

    @ApiModelProperty("教材ID")
    private Long textbookId;
    
    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("发布状态（0:未发布，1:已发布）")
    private Integer status;

    @ApiModelProperty("创建人ID")
    private Long creator;
    
    @ApiModelProperty("创建人名称")
    private String creatorName;

    @ApiModelProperty("创建时间")
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人ID")
    private Long operator;
    
    @ApiModelProperty("操作人名称")
    private String operatorName;

    @ApiModelProperty("操作时间")
    private LocalDateTime operationDatetime;
    
    @ApiModelProperty("关联的教学素材数量")
    private Integer teachingMaterialsCount;
    
    @ApiModelProperty("关联的教学素材列表")
    private List<ApplicationMaterialsDetailVO> teachingMaterials;
}
