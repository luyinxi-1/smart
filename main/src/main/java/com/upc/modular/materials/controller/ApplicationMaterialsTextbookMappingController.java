package com.upc.modular.materials.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsTextbookMappingDto;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsTextbookMappingPageSearchParam;
import com.upc.modular.materials.controller.param.vo.ApplicationMaterialsTextbookMappingReturnParam;
import com.upc.modular.materials.service.IApplicationMaterialsTextbookMappingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @ApiOperation(value = "批量添加教材与应用素材的关联")
    @PostMapping("/insert-mapping-batch/{textbookId}")
    public R<List<Long>> insertMappingBatch(
            @PathVariable Long textbookId,
            @Valid @RequestBody List<ApplicationMaterialsTextbookMappingDto> mappings) {
        try {
            // 直接将textbookId作为参数传入，不需要在这里设置
            List<Long> newIds = applicationMaterialsTextbookMappingService.insertMappingBatch(textbookId, mappings);
            return R.commonReturn(200, "批量添加成功", newIds);
        } catch (BusinessException e) {
            return R.fail(e.getMessage());
        }
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

