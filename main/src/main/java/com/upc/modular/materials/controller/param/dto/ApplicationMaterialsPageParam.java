package com.upc.modular.materials.controller.param.dto;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 应用素材分页查询参数
 * </p>
 *
 * @author system
 * @since 2025-10-29
 */
@Data
@Accessors(chain = true)
@ApiModel("应用素材分页查询参数")
public class ApplicationMaterialsPageParam extends PageBaseSearchParam {

    @ApiModelProperty("应用素材名称（模糊查询）")
    private String name;

    @ApiModelProperty("题库ID")
    private Long questionBankId;

    @ApiModelProperty("发布状态（0:未发布，1:已发布）")
    private Integer status;

    @ApiModelProperty("创建者ID")
    private Long creator;
    
    @ApiModelProperty("只查询当前用户创建的素材（true/false）")
    private Boolean onlyMine;
}