package com.upc.modular;

import com.upc.common.responseparam.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 用于APP测试网络是否正常
 */
@RestController
public class NetworkController {

    @GetMapping("/ping")
    public R ping(HttpServletRequest request) {
        return R.ok();
    }
}
