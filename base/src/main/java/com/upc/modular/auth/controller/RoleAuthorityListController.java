package com.upc.modular.auth.controller;


import com.upc.modular.auth.service.ISysAuthorityService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@RestController
@RequestMapping("/role-authority-list")
@Api(tags = "角色权限关联")
public class RoleAuthorityListController {

    @Autowired
    private ISysAuthorityService sysAuthorityService;



}
