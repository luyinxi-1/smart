package com.upc.modular.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.entity.SysTbuser;
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
public interface SysUserMapper extends BaseMapper<SysTbuser> {

    List<SysTbrole> getRolesByUserId(@Param("userId") Long userId);

    Long getUserInfo(@Param("id") Long id, @Param("userType") Integer userType);
}
