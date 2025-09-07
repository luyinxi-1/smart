package com.upc.modular.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.auth.entity.RoleAuthorityList;
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
public interface RoleAuthorityListMapper extends BaseMapper<RoleAuthorityList> {
    void myDeleteBatch(@Param("deleteList") List<RoleAuthorityList> deleteList);
}
