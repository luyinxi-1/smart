package com.upc.modular.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysLogParam.SysLogPageReturnParam;
import com.upc.modular.auth.controller.param.SysLogParam.SysLogPageSearchParam;
import com.upc.modular.auth.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {

    Page<SysLogPageReturnParam> getLogPage(Page<SysLogPageReturnParam> page, SysLogPageSearchParam param);
}
