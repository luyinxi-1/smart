package com.upc.modular.materials.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.controller.param.dto.BatchMappingRequestDto;
import com.upc.modular.materials.controller.param.dto.MaterialsTextbookMappingDto;
import com.upc.modular.materials.controller.param.dto.MaterialsTextbookMappingPageSearchParam;
import com.upc.modular.materials.controller.param.vo.MaterialsTextbookMappingReturnParam;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsReturnVo;
import com.upc.modular.materials.service.IMaterialsTextbookMappingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author mjh
 * @since 2025-08-23
 */
@Api(tags = "教材素材绑定管理")
@RestController
@RequestMapping("/materials-textbook-mapping")
public class MaterialsTextbookMappingController {

    @Autowired
    private IMaterialsTextbookMappingService materialsTextbookMappingService;

    @ApiOperation(value = "教材素材绑定分页查询")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<MaterialsTextbookMappingReturnParam>> getPage(@RequestBody MaterialsTextbookMappingPageSearchParam param) {
        Page<MaterialsTextbookMappingReturnParam> page = materialsTextbookMappingService.getPage(param);
        PageBaseReturnParam<MaterialsTextbookMappingReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }
    @ApiOperation(value = "添加素材到教材")
    @PostMapping("/insert-mapping")
    public R insertMapping(Long textbookId, Long materialId, String chapterName, Long chapterId, String chapterUuid) {
        Long newId = materialsTextbookMappingService.insertMapping(textbookId, materialId, chapterName, chapterId, chapterUuid);
        if (newId != null) {
            return R.ok(newId);
        }
        return R.fail("添加失败");
    }
    @ApiOperation(value = "批量添加教材与素材的关联")
    @PostMapping("/insert-mapping-batch")
    public R insertMappingBatch(@Valid @RequestBody BatchMappingRequestDto request) {
        // 从name1中提取教材ID（从第一个元素获取）
        Long textbookId = null;
        List<MaterialsTextbookMappingDto> mappings = request.getTextbookMaterialsList();
        if (mappings != null && !mappings.isEmpty()) {
            textbookId = mappings.get(0).getTextbookId();
            // 为每个mapping设置教材ID
            for (MaterialsTextbookMappingDto mapping : mappings) {
                mapping.setTextbookId(textbookId);
            }
        }

        try {
            List<Long> newIds = materialsTextbookMappingService.insertMappingBatchByChapters(textbookId, request.getChapterList(), mappings);
            return R.commonReturn(200, "批量添加成功", newIds);
        } catch (BusinessException e) {
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation(value = "教材素材绑定ID查询教学素材详细信息")
    @PostMapping("/get-materials-by-mappingid")
    public R<TeachingMaterialsReturnVo> getMaterialsByMappingId(Long id) {
        TeachingMaterialsReturnVo result = materialsTextbookMappingService.getMaterialsByMappingId(id);
        return R.ok(result);
    }

    @ApiOperation(value = "删除教材素材绑定")
    @PostMapping("/delete-mapping")
    public R deleteMapping(Long id) {
        if (materialsTextbookMappingService.removeById(id))
            return R.ok("删除成功");
        return R.fail("删除失败");
    }

}