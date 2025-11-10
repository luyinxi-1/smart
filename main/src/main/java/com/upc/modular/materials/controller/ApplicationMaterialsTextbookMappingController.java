package com.upc.modular.materials.controller;

import com.upc.common.responseparam.R;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsTextbookMappingDto;
import com.upc.modular.materials.controller.param.dto.BatchApplicationMaterialsUpdateRequestDto;
import com.upc.modular.materials.service.IApplicationMaterialsTextbookMappingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 应用素材与教材绑定前端控制器
 * </p>
 *
 * @author system
 * @since 2025-10-31
 */
@Api(tags = "应用素材与教材绑定管理")
@RestController
@RequestMapping("/application-materials-textbook-mapping")
public class ApplicationMaterialsTextbookMappingController {

    @Autowired
    private IApplicationMaterialsTextbookMappingService applicationMaterialsTextbookMappingService;

//    @ApiOperation(value = "应用素材教材绑定分页查询")
//    @PostMapping("/getPage")
//    public R<PageBaseReturnParam<ApplicationMaterialsTextbookMappingReturnParam>> getPage(
//            @RequestBody ApplicationMaterialsTextbookMappingPageSearchParam param) {
//        Page<ApplicationMaterialsTextbookMappingReturnParam> page = applicationMaterialsTextbookMappingService.getPage(param);
//        PageBaseReturnParam<ApplicationMaterialsTextbookMappingReturnParam> result = PageBaseReturnParam.ok(page);
//        return R.page(result);
//    }

//    @ApiOperation(value = "添加应用素材到教材")
//    @PostMapping("/insert-mapping")
//    public R<Long> insertMapping(
//            Long textbookId,
//            Long applicationMaterialId,
//            String chapterName,
//            Long chapterId,
//            String chapterUuid) {
//        Long newId = applicationMaterialsTextbookMappingService.insertMapping(
//                textbookId, applicationMaterialId, chapterName, chapterId, chapterUuid);
//        if (newId != null) {
//            return R.ok(newId);
//        }
//        return R.fail("添加失败");
//    }

    @ApiOperation(value = "批量更新教材与应用素材的关联")
    @PostMapping("/insert-mapping-batch")
    public R<List<Long>> insertMappingBatch(@RequestBody BatchApplicationMaterialsUpdateRequestDto request) {
        List<ApplicationMaterialsTextbookMappingDto> mappings = request.getApplicationMaterialsTextbookMappingList();
        List<Long> chapterIds = request.getCatalogList();

        // 当mappings为空但catalogList不为空时，直接删除指定章节的绑定关系
        if ((mappings == null || mappings.isEmpty()) && chapterIds != null && !chapterIds.isEmpty()) {
            // 从章节ID中获取教材ID
            Long textbookId = null;
            // 如果没有提供textbookId，则通过ChapterId查询textbook_id
            if (textbookId == null && !chapterIds.isEmpty()) {
                // 从ChapterList中获取第一个章节ID来查询教材ID
                Long textbookCatalogId = chapterIds.get(0);
                textbookId = applicationMaterialsTextbookMappingService.getTextbookIdByChapterId(textbookCatalogId);
                if (textbookId == null) {
                    return R.commonReturn(200, "删除成功", new java.util.ArrayList<>());
                }
            }
            
            // 删除指定章节绑定的应用素材
            applicationMaterialsTextbookMappingService.removeApplicationMaterialsBindingsByChapterIds(textbookId, chapterIds);
            return R.commonReturn(200, "删除成功", new java.util.ArrayList<>());
        }

        // 从第一个元素中提取教材ID（从第一个元素获取）
        Long textbookId = null;
        if (mappings != null && !mappings.isEmpty()) {
            textbookId = mappings.get(0).getTextbookId();
            // 为每个绑定关系设置教材ID
            for (ApplicationMaterialsTextbookMappingDto mapping : mappings) {
                mapping.setTextbookId(textbookId);
            }
        }

        List<Long> updatedIds = applicationMaterialsTextbookMappingService.updateApplicationMaterialsBatchByChapters(textbookId, chapterIds, mappings);
        return R.commonReturn(200, "批量修改成功", updatedIds);
    }

//    @ApiOperation(value = "删除应用素材教材绑定")
//    @PostMapping("/delete-mapping")
//    public R<String> deleteMapping(Long id) {
//        if (applicationMaterialsTextbookMappingService.removeById(id)) {
//            return R.ok("删除成功");
//        }
//        return R.fail("删除失败");
//    }
}

