package com.upc.modular.questionbank.controller.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 题库素材分页查询参数
 * </p>
 *
 * @author system
 * @since 2025-10-27
 */
@Data
@Accessors(chain = true)
@ApiModel("题库素材分页查询参数")
public class QuestionBankMaterialsPageParam extends PageBaseSearchParam {

    @ApiModelProperty(value = "题库ID", required = true)
    @NotNull(message = "题库ID不能为空")
    private Long questionBankId;

    @ApiModelProperty("素材名称（模糊查询）")
    private String materialName;

    @ApiModelProperty("素材类型（image、video、audio、pdf等）")
    private String materialType;
}
