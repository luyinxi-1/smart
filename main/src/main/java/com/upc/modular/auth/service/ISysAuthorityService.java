package com.upc.modular.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.entity.SysAuthority;
import com.upc.modular.auth.param.SysAuthoritySearchParam;
import com.upc.modular.auth.param.SysAuthorityTreeReturnParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface ISysAuthorityService extends IService<SysAuthority> {

    void deleteSysAuthorityByIds(List<Long> ids);

    R<List<SysAuthorityTreeReturnParam>> getSysAuthorityPage(SysAuthoritySearchParam param);
}
