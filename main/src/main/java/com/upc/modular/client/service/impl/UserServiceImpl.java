package com.upc.modular.client.service.impl;

import com.upc.modular.client.entity.User;
import com.upc.modular.client.mapper.UserMapper;
import com.upc.modular.client.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mjh
 * @since 2025-07-15
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
