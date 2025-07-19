package com.upc.modular.group.mapper;

import com.upc.modular.group.controller.param.GetMyClasssReturnParam;
import com.upc.modular.group.entity.UserClassList;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Mapper
public interface UserClassListMapper extends BaseMapper<UserClassList> {

    GetMyClasssReturnParam getMyClassStudent(@Param("id") Long id);
}
