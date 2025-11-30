package com.upc.modular.materials.controller.param.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * <p>
 * 应用素材保存参数
 * </p>
 *
 * @author system
 * @since 2025-10-29
 */
@Data
@Accessors(chain = true)
@ApiModel("应用素材保存参数")
public class ApplicationMaterialsSaveParam {

    @ApiModelProperty(value = "应用素材ID(更新时必填)")
    private Long id;

    @ApiModelProperty(value = "应用素材名称")
    private String name;

    @ApiModelProperty("应用素材描述")
    private String description;

    @ApiModelProperty("题库ID")
    private Long questionBankId;

    @ApiModelProperty(value = "教材ID")
    private Long textbookId;

    @ApiModelProperty("教材章节ID")
    private Long textbookCatalogId;

    @ApiModelProperty("教材章节ID2（备用）")
    private Long textbookCatalogId2;

    @ApiModelProperty("教材章节UUID（临时ID，当textbookCatalogId为空时使用）")
    private String textbookCatalogUuId;

    @ApiModelProperty("发布状态（0:未发布，1:已发布）")
    private Integer status;
    
    @ApiModelProperty("关联的教学素材ID列表")
    private List<Long> teachingMaterialIds;
}