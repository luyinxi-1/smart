package com.upc.modular.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.param.GetUserIsInInstitutionParam;
import com.upc.modular.auth.param.SysUserPageSearchParam;
import com.upc.modular.auth.param.UpdatePasswordParam;
import com.upc.modular.auth.param.UserLoginParam;
import com.upc.modular.auth.param.UserLoginResultParam;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface ISysUserService extends IService<SysTbuser> {

    UserLoginResultParam login(UserLoginParam userLogin, HttpServletRequest request);

    UserLoginResultParam login1(UserLoginParam userLogin, HttpServletRequest request);

    void batchDelete(List<Long> idList);

    Page<SysTbuser> getPage(SysUserPageSearchParam param);

    Boolean getUserIsInInstitution(GetUserIsInInstitutionParam param);

    Boolean insert(SysTbuser sysTbuser);

    R updatePassword(@RequestBody UpdatePasswordParam param);

    R resetPassword(Long userId);

    Long getUserInfo(Long id, Integer userType);

    String getUserPicture(Long id, Integer userType);
}