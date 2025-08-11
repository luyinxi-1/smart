package com.upc.modular.auth.param.tree;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import upc.c505.modular.auth.entity.SysArea;
import upc.c505.utils.MyBeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author sxz
 * @date 2023/9/21
 **/
@Data
@Accessors(chain = true)
@ApiModel("区域树节点")
public class AreaTreeNode {

    @ApiModelProperty("sysArea表中的id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("子节点")
    private List<AreaTreeNode> areaTreeNodeList;

    @ApiModelProperty("上级地区（父级0为最顶级）")
    @TableField("parent_id")
    private Long parentId;

    @ApiModelProperty("区域类型（1区县,2乡镇街道,3社区,4村/小区/网格）")
    @TableField("area_type")
    private Integer areaType;

    @ApiModelProperty("地区名称")
    @TableField("area_name")
    private String areaName;

    @ApiModelProperty("地图等级（1区县,2乡镇街道,3社区,4村/小区/网格）")
    @TableField("level")
    private String level;

    @ApiModelProperty("显示排序")
    @TableField("seq")
    private Integer seq;

    @ApiModelProperty("经度")
    @TableField("longitude")
    private String longitude;

    @ApiModelProperty("纬度")
    @TableField("latitude")
    private String latitude;

    @ApiModelProperty("状态(0:禁用，1：启用)")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    /**
     * 获取区域节点下所有子节点
     */
    public void getChildren(List<SysArea> allAreaList){
        if(ObjectUtils.isNull(areaTreeNodeList)){
            areaTreeNodeList = new ArrayList<>();
        }
        ListIterator<SysArea> iterator = allAreaList.listIterator();
        while(iterator.hasNext()){
            SysArea next = iterator.next();
            // 如果下一个是当前的子，就加入子节点
            if (next.getParentId().equals(id)) {
                AreaTreeNode treeNode = MyBeanUtils.copy(next, new AreaTreeNode());
                // 子节点再找子节点
                treeNode.getChildren(allAreaList);
                areaTreeNodeList.add(treeNode);
//                // 加入后删除
//                iterator.remove();
            }
        }
    }
}
