package com.upc.modular.client.mapper;

import com.upc.modular.client.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author mjh
 * @since 2025-07-15
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
