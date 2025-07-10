package com.upc.modular.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.entity.UserRoleList;
import com.upc.modular.auth.param.UserRoleListPageReturnParam;
import com.upc.modular.auth.param.UserRoleListPageSearchParam;
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
public interface UserRoleListMapper extends BaseMapper<UserRoleList> {

    Page<UserRoleListPageReturnParam> getPage(@Param("page") Page<UserRoleListPageReturnParam> page,
                                              @Param("param") UserRoleListPageSearchParam param);
}
