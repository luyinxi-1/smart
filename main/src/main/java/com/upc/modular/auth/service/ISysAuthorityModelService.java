package com.upc.modular.auth.service;

import com.upc.modular.auth.entity.SysAuthorityModel;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.auth.param.AuthModelParam;
import com.upc.modular.auth.param.AuthModelTreeNode;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xth
 * @since 2025-07-19
 */
public interface ISysAuthorityModelService extends IService<SysAuthorityModel> {

    void addModel(AuthModelParam authModelParam);

    void deleteModelsByIdList(List<Integer> idList);

    void updateModelById(AuthModelParam authModelParam);

    List<AuthModelTreeNode> getModelPage(Long parentId);
}
