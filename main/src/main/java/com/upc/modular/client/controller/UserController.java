package com.upc.modular.client.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.client.entity.User;
import com.upc.modular.client.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author mjh
 * @since 2025-07-15
 */
@RestController
@RequestMapping("/user")
@Api(tags = "测试模块")
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;

    @ApiOperation("列表")
    @GetMapping("/geto")
    public R<User> geto() {
        User one = userService.getById(1);
        System.out.println(one);
        return R.ok(one);
    }
}
