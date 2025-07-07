package com.upc.modular.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.auth.entity.SysTbrole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysTbrole> {

    /**
     * 获取所有根节点权限
     * @param roleId
     * @return
     */
    List<String> getAccessUrlsByRoleId(@Param("roleId") Long roleId);
}
