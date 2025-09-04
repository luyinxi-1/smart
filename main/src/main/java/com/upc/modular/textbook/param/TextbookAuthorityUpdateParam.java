package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: xth
 * @Date: 2025/9/4 10:30
 */
@Data
public class TextbookAuthorityUpdateParam {

    @ApiModelProperty("权限类型，1表示协作者，2表示可见机构")
    private Integer authorityType;

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("可见机构id集合")
    private List<Long> visibleInstituteIds;

}
