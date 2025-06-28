package com.upc.modular.auth.service.impl;

import com.upc.modular.auth.entity.TestUser;
import com.upc.modular.auth.mapper.TestUserMapper;
import com.upc.modular.auth.service.ITestUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-28
 */
@Service
public class TestUserServiceImpl extends ServiceImpl<TestUserMapper, TestUser> implements ITestUserService {

}
