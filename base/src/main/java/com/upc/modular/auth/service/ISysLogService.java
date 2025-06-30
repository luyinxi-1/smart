package com.upc.modular.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.auth.controller.param.SysLogParam.SysLogPageReturnParam;
import com.upc.modular.auth.controller.param.SysLogParam.SysLogPageSearchParam;
import com.upc.modular.auth.entity.SysLog;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface ISysLogService extends IService<SysLog> {

    Page<SysLogPageReturnParam> getPage(SysLogPageSearchParam param);
}
