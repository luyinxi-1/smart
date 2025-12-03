package com.upc.modular.materials.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsPageParam;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsSaveParam;
import com.upc.modular.materials.controller.param.vo.ApplicationMaterialsVO;
import com.upc.modular.materials.entity.ApplicationMaterials;
import com.upc.modular.materials.entity.ApplicationMaterialsMapping;
import com.upc.modular.materials.entity.ApplicationMaterialsTextbookMapping;
import com.upc.modular.materials.service.IApplicationMaterialsMappingService;
import com.upc.modular.materials.service.IApplicationMaterialsService;
import com.upc.modular.materials.service.IApplicationMaterialsTextbookMappingService;
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
    @Autowired
    private IApplicationMaterialsTextbookMappingService applicationMaterialsTextbookMappingService;

    @Autowired
    private IApplicationMaterialsMappingService applicationMaterialsMappingService;


    @ApiOperation("新增应用素材")
    @PostMapping("/add")
    public R<Long> addApplicationMaterials(@RequestBody ApplicationMaterialsSaveParam param) {
        // 设置状态默认为1（发布）
        if (param.getStatus() == null) {
            param.setStatus(1);
        }
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
    @ApiOperation("【客户端】根据教材id下载应用素材主表")
    @GetMapping("/downloadByTextbookId")
    public R<List<ApplicationMaterials>> downloadByTextbookId(@RequestParam Long textbookId) {
        // 通过 application_materials_textbook 关联查出该教材下所有应用素材
        List<ApplicationMaterials> list = applicationMaterialsService.listByTextbookId(textbookId);
        return R.ok(list);
    }

    @ApiOperation("【客户端】根据教材id下载应用素材-教材关联表")
    @GetMapping("/downloadTextbookMappingByTextbookId")
    public R<List<ApplicationMaterialsTextbookMapping>> downloadTextbookMappingByTextbookId(@RequestParam Long textbookId) {
        List<ApplicationMaterialsTextbookMapping> list = applicationMaterialsTextbookMappingService.listByTextbookId(textbookId);
        return R.ok(list);
    }

    @ApiOperation("【客户端】根据教材id下载应用素材-教学素材关联表")
    @GetMapping("/downloadMappingByTextbookId")
    public R<List<ApplicationMaterialsMapping>> downloadMappingByTextbookId(@RequestParam Long textbookId) {
        List<ApplicationMaterialsMapping> list = applicationMaterialsMappingService.listByTextbookId(textbookId);
        return R.ok(list);
    }


}
