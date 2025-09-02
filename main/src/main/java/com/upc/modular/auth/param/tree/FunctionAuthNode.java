package com.upc.modular.auth.param.tree;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.upc.modular.auth.entity.SysAuthorityModel;
import com.upc.modular.auth.utils.MyBeanUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * @author sxz
 * @date 2023/8/22
 **/
@Data
@ApiModel("功能权限")
@Accessors(chain = true)
public class FunctionAuthNode {

    @ApiModelProperty("适用区域id")
    private Long areaId;

    @ApiModelProperty("适用区域name")
    private String areaName;

    @ApiModelProperty("子节点")
    private List<FunctionAuthNode> functionAuthNodeList;

    @ApiModelProperty("节点id")
    private Long id;

    @ApiModelProperty("父节点id")
    private Long parentId;

    @ApiModelProperty("顺序")
    private Integer seq;

    @ApiModelProperty("节点类型,0:权限模块，1：菜单，2：按钮，3：其他")
    private Integer type;

    @ApiModelProperty("url")
    private String url;

    @ApiModelProperty("权限名")
    private String authName;

    @ApiModelProperty("图片链接")
    private String picUrl;

    @ApiModelProperty("是否外链")
    private Integer haveUrl;

    public void getChildrenNode(List<SysAuthorityModel> sysAuthModels, ConcurrentMap<Long, List<FunctionAuthNode>> concurrentMap) {
        for (SysAuthorityModel authModel : sysAuthModels) {
            //找到子节点
            if (Objects.equals(authModel.getParentId(), id)) {
                FunctionAuthNode currentAuthNode = MyBeanUtils.copy(authModel, new FunctionAuthNode())
                        .setAuthName(authModel.getAuthModelName())
                        .setType(0);
                currentAuthNode.getChildrenNode(sysAuthModels, concurrentMap);
                //没有子节点了，找权限点节点
                if (CollectionUtils.isEmpty(currentAuthNode.getFunctionAuthNodeList())) {
                    currentAuthNode.setFunctionAuthNodeList(concurrentMap.get(currentAuthNode.getId()));
                }
                //如果权限节点也没有说明不是该用户的权限
                if (CollectionUtils.isNotEmpty(currentAuthNode.getFunctionAuthNodeList())) {
                    if (Objects.isNull(functionAuthNodeList)) {
                        this.functionAuthNodeList = new ArrayList<>();
                    }
                    this.functionAuthNodeList.add(currentAuthNode);
                    this.functionAuthNodeList.sort(Comparator.comparing(FunctionAuthNode::getSeq));
                }
            }
        }
    }
}
