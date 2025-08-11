package com.upc.modular.auth.param.tree;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author sxz
 * @date 2023/9/23
 **/
@Data
@Accessors(chain = true)
@ApiModel("区域树节点")
public class AreaTreeIteratorNode {

    @ApiModelProperty("sysArea表中的id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("上级地区（父级0为最顶级）")
    @TableField("parent_id")
    private Long parentId;

    @ApiModelProperty("区域类型（1区县,2乡镇街道,3社区,4村/小区/网格）")
    @TableField("area_type")
    private Integer areaType;

    @ApiModelProperty("地区名称")
    @TableField("area_name")
    private String areaName;

    @ApiModelProperty("显示排序")
    @TableField("seq")
    private Integer seq;

    @ApiModelProperty("行政编号")
    @TableField("administrative_number")
    private String administrativeNumber;

    @ApiModelProperty("状态(0:禁用，1：启用)")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty("是否已经遍历过")
    private Integer isPicked;
}
