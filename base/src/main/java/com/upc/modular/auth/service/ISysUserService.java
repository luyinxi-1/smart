package com.upc.modular.auth.service;

import com.upc.modular.auth.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.auth.param.UserLoginParam;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * WEB端用户登录
     * @return 返回登陆成功的token
     */
    String login(UserLoginParam userLogin, HttpServletRequest request);
}
