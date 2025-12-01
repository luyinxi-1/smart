package com.upc.modular.client.controller;

import com.upc.common.responseparam.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Api(tags = "健康检查")
public class HealthController {
    @ApiOperation("服务健康检查")
    @GetMapping("/check")
    public R<String> check() {
        // 不做任何复杂逻辑，只要接口能正常返回，就说明服务端在线
        return R.ok("OK");
    }
}