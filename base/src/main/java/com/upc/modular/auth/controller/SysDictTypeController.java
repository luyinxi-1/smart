package com.upc.modular.auth.controller;


import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.controller.param.SysDictTypeParam.SysDictTypeSearchParam;
import com.upc.modular.auth.entity.SysDictType;
import com.upc.modular.auth.service.ISysDictTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation(value = "添加字典类型")
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

    @ApiOperation(value = "删除字典类型")
    @PostMapping("/batchDelete")
    public R batchDelete(@RequestBody IdParam idParam) {
        dictTypeService.deleteDictTypeByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "查询字典类型")
    @PostMapping("/getById")
    public R getById(@RequestParam("dictId") Integer dictId) {
        SysDictType dictType = dictTypeService.getById(dictId);
        return R.commonReturn(200, "查询成功", dictType);
    }

    @ApiOperation(value = "修改字典类型")
    @PostMapping("/update")
    public R update(@RequestBody SysDictType dict) {
        dictTypeService.updateDictType(dict);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "检查数据字典类型是否已存在")
    @PostMapping("/checkDictTypeUnique")
    public String checkDictTypeUnique(SysDictType param) {
        return dictTypeService.checkDictTypeUnique(param);
    }

    @ApiOperation(value = "分页按条件查询字典类型")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<SysDictType>> getPage(@RequestBody SysDictTypeSearchParam dictType) {
        return dictTypeService.selectDictTypeList(dictType);
    }



}
