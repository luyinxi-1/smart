package com.upc.modular.auth.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.auth.entity.SysDictItem;
import com.upc.modular.auth.service.ISysDictItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@RestController
@RequestMapping("/sys-dict-item")
@Api(tags = "字典项")
public class SysDictItemController {

    @Autowired
    private ISysDictItemService sysDictItemService;

    @ApiOperation(value = "添加字典项")
    @PostMapping("/insert")
    public R insert(@RequestBody SysDictItem dictItem) {
        boolean result = sysDictItemService.insertDictItem(dictItem);
        return R.commonReturn(200, "新增成功", "");
    }

}
