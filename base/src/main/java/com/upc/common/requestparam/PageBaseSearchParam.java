package com.upc.common.requestparam;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PageBaseSearchParam {

    @ApiModelProperty("每页显示条数，默认 10")
    private Long size = 10L;

    @ApiModelProperty("当前页，默认为第一页")
    private Long current = 1L;

    @ApiModelProperty("是否升序排列（默认为0）")
    private Integer isAsc = 0;

    @ApiModelProperty("是否查询适用区域（0：查询管辖区域），1：查询适用区域")
    private Integer isApplicableArea;

    @ApiModelProperty("所属区域（适用区域）id")
    private Long areaId;

    @ApiModelProperty("查询标识")
    private Integer flag;
}
