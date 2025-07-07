package com.upc.modular.auth.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.auth.entity.TeachingMaterial;
import com.upc.modular.auth.service.ITeachingMaterialService;
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
@RequestMapping("/teaching-material")
@Api(tags = "附件管理")
public class TeachingMaterialController {

    @Autowired
    private ITeachingMaterialService teachingMaterialService;

    @ApiOperation(value = "新增附件")
    @PostMapping("/insertTeachingMaterial")
    public R insertTeachingMaterial(@RequestBody TeachingMaterial teachingMaterial) {
        return R.ok(teachingMaterialService.save(teachingMaterial));
    }

    @ApiOperation(value = "按ID删除附件")
    @PostMapping("/deleteTeachingMaterialById")
    public R deleteTeachingMaterialById(@RequestBody Long id) {
        return R.ok(teachingMaterialService.removeById(id));
    }

    @ApiOperation(value = "修改附件")
    @PostMapping("/updateTeachingMaterialById")
    public R updateTeachingMaterialById(@RequestBody TeachingMaterial teachingMaterial) {
        return R.ok(teachingMaterialService.updateTeachingMaterial(teachingMaterial));
    }

    @ApiOperation(value = "附件列表查询")
    @PostMapping("/getTeachingMaterialList")
    public R getTeachingMaterialList() {
        return R.ok(teachingMaterialService.list());
    }

}
