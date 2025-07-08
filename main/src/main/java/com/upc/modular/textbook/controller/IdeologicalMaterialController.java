package com.upc.modular.textbook.controller;


import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.teachingActivities.entity.DiscussionTopic;
import com.upc.modular.teachingActivities.param.DiscussionTopicSearchParam;
import com.upc.modular.textbook.entity.IdeologicalMaterial;
import com.upc.modular.textbook.param.IdeologicalMaterialSearchParam;
import com.upc.modular.textbook.service.IIdeologicalMaterialService;
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
 * @since 2025-07-08
 */
@RestController
@RequestMapping("/ideological-material")
@Api(tags = "教学思政")
public class IdeologicalMaterialController {

    @Autowired
    private IIdeologicalMaterialService iIdeologicalMaterialService;

    @ApiOperation(value = "删除教学思政")
    @PostMapping("/deleteIdeologicalMaterialByIds")
    public R deleteIdeologicalMaterialByIds(@RequestBody List<Long> ids) {
        iIdeologicalMaterialService.deleteIdeologicalMaterialByIds(ids);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "新增教学教学思政")
    @PostMapping("/insertIdeologicalMaterial")
    public R insertIdeologicalMaterial(@RequestBody IdeologicalMaterial ideologicalMaterial) {
        iIdeologicalMaterialService.insertIdeologicalMaterial(ideologicalMaterial);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "查询教学思政信息")
    @PostMapping("/getIdeologicalMaterialById")
    public R getIdeologicalMaterialById(@RequestParam Long id) {
        if (id == null || id == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        IdeologicalMaterial ideologicalMaterial = iIdeologicalMaterialService.getById(id);
        return R.commonReturn(200, "查询成功", ideologicalMaterial);
    }

    @ApiOperation(value = "修改教学思政")
    @PostMapping("/updateIdeologicalMaterialById")
    public R updateIdeologicalMaterialById(@RequestBody IdeologicalMaterial ideologicalMaterial) {
        iIdeologicalMaterialService.updateIdeologicalMaterialById(ideologicalMaterial);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "按条件查询教学思政")
    @PostMapping("/getIdeologicalMaterialByConditions")
    public R<List<IdeologicalMaterial>> getIdeologicalMaterialByConditions(@RequestBody IdeologicalMaterialSearchParam param) {
        List<IdeologicalMaterial> ideologicalMaterialList = iIdeologicalMaterialService.getIdeologicalMaterialByConditions(param);
        return R.ok(ideologicalMaterialList);
    }
}
