package com.upc.modular.textbook.controller;

import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.textbook.entity.IdeologicalMaterial;
import com.upc.modular.textbook.entity.MaterialPush;
import com.upc.modular.textbook.param.*;
import com.upc.modular.textbook.service.IMaterialPushService;
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
 * @author MJ
 * @since 2025-10-28
 */
@RestController
@RequestMapping("/material-push")
@Api(tags = "资料推送")
public class MaterialPushController {
    @Autowired
    private IMaterialPushService iMaterialPushService;
    @ApiOperation(value = "新增资料推送")
    @PostMapping("/insertPushMaterial")
    public R<Long> insertIdeologicalMaterial(@RequestBody PushMaterialInsertAndUpdateParam param) {
        Long id = iMaterialPushService.insertPushMaterial(param);
        return R.ok(id);
    }

    @ApiOperation(value = "删除资料推送")
    @PostMapping("/deleteIdeologicalMaterialByIds")
    public R deleteIdeologicalMaterialByIds(@RequestBody List<Long> ids) {
        iMaterialPushService.deleteIdeologicalMaterialByIds(ids);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "查询推送资料")
    @PostMapping("/getPushMaterialById")
    public R getPushMaterialById(@RequestParam Long id) {
        if (id == null || id == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        PushMaterialInsertAndUpdateParam pushMaterial = iMaterialPushService.getMaterialById(id);
        return R.commonReturn(200, "查询成功",pushMaterial);
    }

    @ApiOperation(value = "根据教材id查询推送资料")
    @PostMapping("/getPushMaterialByTextbookId")
    public R getPushMaterialByTextbookId(@RequestParam MaterialPushPageSearchParam param) {
        if (param.getTextbookId() == null || param.getTextbookId() == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        return R.ok(iMaterialPushService.getPushMaterialByTextbookIdPage(param));
    }

    @ApiOperation(value = "修改推送资料")
    @PostMapping("/updatePushMaterialById")
    public R updatePushMaterialById(@RequestBody PushMaterialInsertAndUpdateParam param) {
        iMaterialPushService.updatePushMaterialById(param);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "批量更新推送资料")
    @PostMapping("/batchUpdatePushMaterial")
    public R batchUpdatePushMaterial(@RequestBody List<PushMaterialBatchUpdateCatalogParam> params) {
        iMaterialPushService.batchUpdateCatalog(params);
        return R.commonReturn(200, "更新成功", "");
    }



}
