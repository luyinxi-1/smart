package com.upc.modular.auth.controller.param.SysDictItemParam;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SysDictItemPageSearchParam extends PageBaseSearchParam {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("字典编码")
    private Integer id;

    @ApiModelProperty("字典唯一编码")
    private String dictTypeCode;

    @ApiModelProperty("状态（0正常 1停用）")
    private String status;

    @ApiModelProperty("字典项值")
    @TableField("dict_item_value")
    private String dictItemValue;

    @ApiModelProperty("字典项名称")
    @TableField("dict_item_name")
    private String dictItemName;


}
