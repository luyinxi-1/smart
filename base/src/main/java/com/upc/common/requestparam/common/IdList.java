package com.upc.common.requestparam.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;
@Data
public class IdList {
    @ApiModelProperty("id列表")
    private List<T> idList;
}
