package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 * 题库素材关联参数
 * </p>
 *
 * @author cyy
 * @since 2025-10-27
 */
@Data
@ApiModel("题库素材关联参数")
public class QuestionBankMaterialsParam {

    @ApiModelProperty(value = "题库ID", required = true)
    @NotNull(message = "题库ID不能为空")
    private Long questionBankId;

    @ApiModelProperty(value = "素材ID列表", required = true)
    @NotNull(message = "素材ID列表不能为空")
    private List<Long> materialIds;
}

