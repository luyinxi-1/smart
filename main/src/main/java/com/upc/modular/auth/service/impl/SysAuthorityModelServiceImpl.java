package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysAuthority;
import com.upc.modular.auth.entity.SysAuthorityModel;
import com.upc.modular.auth.mapper.SysAuthorityMapper;
import com.upc.modular.auth.mapper.SysAuthorityModelMapper;
import com.upc.modular.auth.param.AuthModelParam;
import com.upc.modular.auth.param.AuthModelTreeNode;
import com.upc.modular.auth.service.ISysAuthorityModelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.auth.utils.MyBeanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xth
 * @since 2025-07-19
 */
@Service
public class SysAuthorityModelServiceImpl extends ServiceImpl<SysAuthorityModelMapper, SysAuthorityModel> implements ISysAuthorityModelService {

    @Autowired
    private SysAuthorityModelMapper sysAuthorityModelMapper;

    @Autowired
    private SysAuthorityMapper sysAuthorityMapper;

    @Override
    public void addModel(AuthModelParam authModelParam) {
        SysAuthorityModel sysAuthModel = new SysAuthorityModel();
        BeanUtils.copyProperties(authModelParam, sysAuthModel);
        sysAuthorityModelMapper.insert(sysAuthModel);
    }

    @Override
    public void deleteModelsByIdList(List<Integer> idList) {
        //先删除模块
        sysAuthorityModelMapper.deleteBatchIds(idList);
        //删除模块下的权限
        sysAuthorityMapper.delete(new MyLambdaQueryWrapper<SysAuthority>()
                .in(SysAuthority::getAuthModelId, idList)
        );
    }

    @Override
    public void updateModelById(AuthModelParam authModelParam) {
        if (authModelParam.getId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, ":更新时id不能为空");
        }
        SysAuthorityModel sysAuthModel = new SysAuthorityModel();
        BeanUtils.copyProperties(authModelParam, sysAuthModel);
        sysAuthorityModelMapper.updateById(sysAuthModel);
        // 如果改模块名，把权限对应的auth_model_name也改了
        if(ObjectUtils.isNotNull(authModelParam.getAuthModelName())){
            sysAuthorityMapper.update(
                    new SysAuthority().setAuthModelName(authModelParam.getAuthModelName()),
                    new LambdaUpdateWrapper<SysAuthority>()
                            .eq(SysAuthority::getAuthModelId,authModelParam.getId())
            );
        }

    }

    @Override
    public List<AuthModelTreeNode> getModelPage(Long parentId) {
        List<SysAuthorityModel> topAuthModels = sysAuthorityModelMapper.selectList(
                new MyLambdaQueryWrapper<SysAuthorityModel>()
                        .eq(SysAuthorityModel::getParentId, parentId)
                        .orderBy(true,true,SysAuthorityModel::getSeq)
        );
        List<SysAuthorityModel> allAuthModels = sysAuthorityModelMapper.selectList(
                new MyLambdaQueryWrapper<SysAuthorityModel>()
                        .orderBy(true,true,SysAuthorityModel::getSeq)
        );
        List<AuthModelTreeNode> authModelTreeNodeList = topAuthModels.stream().map(item -> {
            AuthModelTreeNode authModelTreeNode = MyBeanUtils.copy(item, new AuthModelTreeNode());
            authModelTreeNode.getChildren(allAuthModels);
            return authModelTreeNode;
        }).collect(Collectors.toList());
        return authModelTreeNodeList;
    }


}
