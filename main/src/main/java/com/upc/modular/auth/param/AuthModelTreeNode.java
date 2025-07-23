package com.upc.modular.auth.param;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.modular.auth.entity.SysAuthorityModel;
import com.upc.modular.auth.utils.MyBeanUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@Data
@Accessors(chain = true)
@ApiModel("权限模块树")
public class AuthModelTreeNode {

    private Long id;

    @ApiModelProperty("子节点")
    private List<AuthModelTreeNode> authModelTreeNodeList;

    @ApiModelProperty("上级模块id")
    private Long parentId;

    @ApiModelProperty("权限模块名称")
    private String authModelName;

    @ApiModelProperty("顺序")
    private Integer seq;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("是否外链")
    private Integer haveUrl;

    @ApiModelProperty("外链")
    private String url;

    @ApiModelProperty("图片链接")
    private String picUrl;

    public void getChildren(List<SysAuthorityModel> allAuthModels) {
        if (ObjectUtils.isNull(authModelTreeNodeList)) {
            authModelTreeNodeList = new ArrayList<>();
        }
        ListIterator<SysAuthorityModel> sysAuthModelListIterator = allAuthModels.listIterator();
        while (sysAuthModelListIterator.hasNext()) {
            SysAuthorityModel next = sysAuthModelListIterator.next();
            //如果下一个是当前的子，就加入子节点
            if (next.getParentId().equals(id)) {
                AuthModelTreeNode treeNode = MyBeanUtils.copy(next, new AuthModelTreeNode());
                //子节点再找子节点
                treeNode.getChildren(allAuthModels);
                authModelTreeNodeList.add(treeNode);
                //加入后删除
//                sysAuthModelListIterator.remove();
            }
        }
    }
}