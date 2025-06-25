package com.upc.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.entity.TestUser;
import com.upc.mapper.TestUserMapper;
import com.upc.service.TestUserService;
import org.springframework.stereotype.Service;

@Service
public class TestUserServiceImpl extends ServiceImpl<TestUserMapper, TestUser> implements TestUserService {

}
