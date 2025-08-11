package com.upc.modular.homepage.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.homepage.entity.MainImageConfiguration;
import com.upc.modular.homepage.mapper.MainImageConfigurationMapper;
import com.upc.modular.homepage.service.IMainImageConfigurationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-08-11
 */
@Service
public class MainImageConfigurationServiceImpl extends ServiceImpl<MainImageConfigurationMapper, MainImageConfiguration> implements IMainImageConfigurationService {

    @Autowired
    private MainImageConfigurationMapper mainImageConfigurationMapper;
    @Override
    public Boolean insert(MainImageConfiguration param) {
        if (ObjectUtils.isEmpty(param)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        return this.save(param);
    }

    @Override
    public Boolean batchDelete(IdParam idParam) {
        if (ObjectUtils.isEmpty(idParam) || ObjectUtils.isEmpty(idParam.getIdList())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        return this.removeByIds(idParam.getIdList());
    }

    @Override
    public Boolean updateConfiguration(MainImageConfiguration param) {
        if (ObjectUtils.isEmpty(param) || ObjectUtils.isEmpty(param)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.updateById(param);
    }

    @Override
    public List<MainImageConfiguration> selectALlConfiguration() {
        MyLambdaQueryWrapper<MainImageConfiguration> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(MainImageConfiguration::getIsTop)
                .orderByDesc(MainImageConfiguration::getSortOrder);
        return mainImageConfigurationMapper.selectList(lambdaQueryWrapper);

    }
}
