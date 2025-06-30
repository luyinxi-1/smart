package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysLogParam.SysLogPageReturnParam;
import com.upc.modular.auth.controller.param.SysLogParam.SysLogPageSearchParam;
import com.upc.modular.auth.entity.SysLog;
import com.upc.modular.auth.mapper.SysLogMapper;
import com.upc.modular.auth.service.ISysLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements ISysLogService {

    @Autowired
    private SysLogMapper sysLogMapper;
    @Override
    public Page<SysLogPageReturnParam> getPage(SysLogPageSearchParam param) {
        Page<SysLogPageReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        return sysLogMapper.getLogPage(page, param);
    }
}
