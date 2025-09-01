package com.upc.modular.client.controller;

import com.upc.common.responseparam.R;
import com.upc.modular.client.controller.param.SyncInfoReturnParam;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "数据同步接口")
@RestController
@RequestMapping("/dataSync")
public class DataSyncController {

    @PostMapping("/learningLog")
    public R<SyncInfoReturnParam> learningLog(@RequestBody ArrayList<Object> params) {
        System.out.println("params: " + params);
        // 创建Map<String, String>
        Map<String, String> map = new HashMap<>();
        map.put("测试失败", "value");
        // 创建List<Map<String, String>>
        List<Map<String, String>> list = new ArrayList<>();
        list.add(map);
        SyncInfoReturnParam returnParam = new SyncInfoReturnParam()
                .setSyncInfo("同步成功")
                .setSyncTime(LocalDateTime.now())
                .setSyncUserId("1")
                .setFailList(list);
        return R.ok(returnParam);
    }
}
