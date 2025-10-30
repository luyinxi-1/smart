package com.upc.modular.materials.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsPageParam;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsSaveParam;
import com.upc.modular.materials.controller.param.vo.ApplicationMaterialsVO;
import com.upc.modular.materials.service.IApplicationMaterialsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 应用素材管理控制器
 * </p>
 *
 * @author system
 * @since 2025-10-29
 */
@RestController
@RequestMapping("/application-materials-manage")
@Api(tags = "应用素材管理")
public class ApplicationMaterialsManageController {

    @Autowired
    private IApplicationMaterialsService applicationMaterialsService;

    @ApiOperation("新增应用素材")
    @PostMapping("/add")
    public R<Long> addApplicationMaterials(@RequestBody ApplicationMaterialsSaveParam param) {
        Long id = applicationMaterialsService.saveApplicationMaterials(param);
        return R.ok(id);
    }

    @ApiOperation("修改应用素材")
    @PostMapping("/update")
    public R<String> updateApplicationMaterials(@RequestBody ApplicationMaterialsSaveParam param) {
        boolean success = applicationMaterialsService.updateApplicationMaterials(param);
        return success ? R.ok("更新成功") : R.commonReturn(500, "更新失败", null);
    }

    @ApiOperation("删除应用素材")
    @PostMapping("/batch-delete")
    public R<String> batchDeleteApplicationMaterials(@RequestBody List<Long> ids) {
        boolean success = applicationMaterialsService.batchDeleteApplicationMaterials(ids);
        return success ? R.ok("批量删除成功") : R.commonReturn(500, "批量删除失败", null);
    }

    @ApiOperation("查询单个应用素材详情")
    @PostMapping("/{id}")
    public R<ApplicationMaterialsVO> getApplicationMaterialsDetail(
            @ApiParam("应用素材ID") @PathVariable Long id,
            @ApiParam("是否包含关联的教学素材") @RequestParam(defaultValue = "true") boolean includeTeachingMaterials) {
        ApplicationMaterialsVO result = applicationMaterialsService.getApplicationMaterialsById(id, includeTeachingMaterials);
        return R.ok(result);
    }

    @ApiOperation("分页查询应用素材")
    @PostMapping("/page")
    public R<PageBaseReturnParam<ApplicationMaterialsVO>> getApplicationMaterialsPage(@RequestBody ApplicationMaterialsPageParam param) {
        Page<ApplicationMaterialsVO> page = applicationMaterialsService.getApplicationMaterialsPage(param);
        PageBaseReturnParam<ApplicationMaterialsVO> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }
}
