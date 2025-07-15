package com.upc.modular.teachingactivities.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/7/7 20:44
 */
@Data
public class LikeStateParam {

    @ApiModelProperty("关联类型（1：教学活动；2:回复）")
    private Integer type;

    @ApiModelProperty("关联的教学活动或回复id")
    private Long correlationId;

    @ApiModelProperty("点赞人")
    private Long creator;
}
