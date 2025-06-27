package com.upc.modular.auth.controller;


import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysDictType;
import com.upc.modular.auth.service.ISysDictTypeService;
import io.swagger.annotations.Api;
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
@RequestMapping("/sys-dict-type")
@Api(tags = "字典类型")
public class SysDictTypeController {

    @Autowired
    private ISysDictTypeService dictTypeService;

    @ApiOperation(value = "添加数据字典类型")
    @PostMapping("/insert")
    public R insert(@RequestBody SysDictType param) {
        if ("0".equals(dictTypeService.checkDictTypeUnique(param))) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "新增字典'" + param.getDictTypeName() + "'失败，字典类型已存在");
        }
        boolean save = dictTypeService.save(param);
        if (save) {
            return R.commonReturn(200, "新增成功", "");
        }
        return R.commonReturn(400, "新增失败", "");
    }

    @ApiOperation(value = "检查数据字典类型是否已存在")
    @PostMapping("/checkDictTypeUnique")
    public String checkDictTypeUnique(SysDictType param) {
        return dictTypeService.checkDictTypeUnique(param);
    }

}
