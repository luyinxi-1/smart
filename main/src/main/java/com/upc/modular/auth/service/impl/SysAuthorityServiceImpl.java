package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.RoleAuthorityList;
import com.upc.modular.auth.entity.SysAuthority;
import com.upc.modular.auth.mapper.SysAuthorityMapper;
import com.upc.modular.auth.param.SysAuthoritySearchParam;
import com.upc.modular.auth.service.ISysAuthorityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Service
public class SysAuthorityServiceImpl extends ServiceImpl<SysAuthorityMapper, SysAuthority> implements ISysAuthorityService {

    @Override
    public void deleteSysAuthorityByIds(List<Long> ids) {
        if(CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        this.removeBatchByIds(ids);
    }

    @Override
    public R<Page<SysAuthority>> getSysAuthorityPage(SysAuthoritySearchParam param) {
        Page<SysAuthority> pageInfo = new Page(param.getCurrent(), param.getSize());
        Page<SysAuthority> page = this.page(pageInfo,
                new LambdaQueryWrapper<SysAuthority>()
                        .like(StringUtils.isNotBlank(param.getAccessUrl()), SysAuthority::getAccessUrl, param.getAccessUrl())
                        .orderBy(true, Objects.equals(1, param.getIsAsc()), SysAuthority::getAddDatetime)
        );

        return R.ok(page);
    }
}
