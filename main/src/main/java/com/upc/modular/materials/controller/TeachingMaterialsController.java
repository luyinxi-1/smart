package com.upc.modular.materials.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.materials.controller.param.dto.TeachingMaterialsPageSearchDto;
import com.upc.modular.materials.controller.param.dto.TeachingMaterialsSaveOrUpdateParam;
import com.upc.modular.materials.controller.param.vo.MaterialsTextbookNameMappingReturnParam;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsInsertMaterialsReturnParam;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsReturnVo;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author mjh
 * @since 2025-07-17
 */
@Api(tags = "教学素材管理")
@RestController
@RequestMapping("/teaching-materials")
public class TeachingMaterialsController {

    @Autowired
    private ITeachingMaterialsService teachingMaterialsService;

    @ApiOperation(value = "添加教学素材")
    @PostMapping("/insert-materials")
    public R<TeachingMaterialsInsertMaterialsReturnParam> insertMaterials(@RequestBody TeachingMaterialsSaveOrUpdateParam param) {
        TeachingMaterialsInsertMaterialsReturnParam result = teachingMaterialsService.insertMaterials(param);
        if (result == null) {
            return R.fail("修改失败");
        }
        return new R<TeachingMaterialsInsertMaterialsReturnParam>(200, "添加教学素材成功", result);
    }
    @ApiOperation(value = "下载教学素材")
    @GetMapping("/download-file-materials")
    public void downloadFileMaterials(@RequestParam Long id, @RequestParam Integer imageSetId, @RequestParam Long textbookId, @RequestParam String action, HttpServletResponse response) {
        teachingMaterialsService.getFileMaterials(id, imageSetId, textbookId, action, response);
    }
    @ApiOperation(value = "查看链接素材")
    @GetMapping("/get-link-materials")
    public R<String> getLinkMaterials(@RequestParam Long id, @RequestParam Long textbookId) {
        String url = teachingMaterialsService.getLinkMaterials(id, textbookId);
        return R.ok(url);
    }
    @ApiOperation(value = "分页查询教学素材")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<TeachingMaterialsReturnVo>> getPage(@RequestBody TeachingMaterialsPageSearchDto param) {
        Page<TeachingMaterialsReturnVo> page = teachingMaterialsService.getPage(param);
        PageBaseReturnParam<TeachingMaterialsReturnVo> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }
@ApiOperation(value = "查看课本所绑定的教学素材")
@GetMapping("/get-textbookId-materials")
public R<List<TeachingMaterials>> getMaterialsForTextbook(
        @RequestParam Long textbookId,
        @RequestParam(required = false) String materialName) {
    List<TeachingMaterials> materials = teachingMaterialsService.getMaterialsByTextbookId(textbookId, materialName);
    return R.ok(materials);
}
    @ApiOperation(value = "查看教学素材(学生查看时用到textbookId)")
    @GetMapping("/get-teaching-materials")
    public R<TeachingMaterialsReturnVo> get(@RequestParam Long id, @RequestParam Long textbookId) {
        TeachingMaterialsReturnVo teachingMaterials = teachingMaterialsService.getTeachingMaterials(id, textbookId);
        return R.ok(teachingMaterials);
    }
    @ApiOperation(value = "修改教学素材信息")
    @PostMapping("/updateTeachingMaterialsById")
    public R<String> updateInstitutionById(@RequestBody TeachingMaterialsSaveOrUpdateParam param) {
        String result = teachingMaterialsService.updateTeachingMaterialsById(param);
        if (result == null)
            return R.fail("修改失败");
        else return R.ok(result);
    }
    @ApiOperation(value = "删除教学素材信息")
    @PostMapping("/deleteTeachingMaterialsByIds")
    public R deleteTeachingMaterialsByIds(@RequestBody List<Long> ids) {
        teachingMaterialsService.deleteTeachingMaterialsByIds(ids);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "获取素材关联教材（可用于删除素材前确认）")
    @PostMapping("/getMaterialsTextbookMappingByMaterialsId")
    public R<MaterialsTextbookNameMappingReturnParam> getMaterialsTextbookMappingByMaterialsId(@RequestBody List<Long> ids) {
        MaterialsTextbookNameMappingReturnParam result = teachingMaterialsService.getMaterialsTextbookMappingByMaterialsId(ids);
        return R.ok(result);
    }
}
