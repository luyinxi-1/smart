package com.upc.modular.auth.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictItemParam.SysDictItemPageSearchParam;
import com.upc.modular.auth.controller.param.SysDictItemParam.SysDictItemSearchParam;
import com.upc.modular.auth.controller.param.SysDictItemParam.SysDictItemTotalParam;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysDictData;
import com.upc.modular.auth.service.ISysDictDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
public class SysDictDataController {

    @Autowired
    private ISysDictDataService sysDictItemService;

    @ApiOperation(value = "添加字典项")
    @PostMapping("/insert")
    public R insert(@RequestBody SysDictData dictItem) {
        boolean result = sysDictItemService.insertDictItem(dictItem);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "删除字典类型")
    @PostMapping("/batchDelete")
    public R batchDelete(@RequestBody IdParam idParam) {
        sysDictItemService.deleteDictItemByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "修改字典类型")
    @PostMapping("/update")
    public R update(@RequestBody SysDictData dict) {
        sysDictItemService.updateById(dict);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "下拉框查询")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<SysDictData>> getPage(@RequestBody SysDictItemPageSearchParam param) {
        Page<SysDictData> page = sysDictItemService.getPage(param);
        PageBaseReturnParam<SysDictData> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "根据字典数据类型获取字典数据")
    @PostMapping("/selectDictDataByDictType")
    public R selectDictDataByDictType(@RequestBody SysDictItemSearchParam param) {
        List<SysDictData> list = sysDictItemService.selectDictDataByDictType(param);
        SysDictItemTotalParam result = new SysDictItemTotalParam();
        result.setSysDictDataList(list);
        result.setTotalNum(list.size());
        return R.commonReturn(200, "查询成功", result);
    }


}
