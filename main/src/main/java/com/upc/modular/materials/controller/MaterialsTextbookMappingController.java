package com.upc.modular.materials.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.materials.controller.param.dto.MaterialsTextbookMappingPageSearchParam;
import com.upc.modular.materials.controller.param.vo.MaterialsTextbookMappingReturnParam;
import com.upc.modular.materials.service.IMaterialsTextbookMappingService;
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
    public R insertMapping(Long textbookId, Long materialId, String chapterName, Integer chapterId) {
        if (materialsTextbookMappingService.insertMapping(textbookId, materialId, chapterName, chapterId))
            return R.ok("添加成功");
        return R.fail("添加失败");
    }

    @ApiOperation(value = "删除教材素材绑定")
    @PostMapping("/delete-mapping")
    public R deleteMapping(Long id) {
        if (materialsTextbookMappingService.removeById(id))
            return R.ok("删除成功");
        return R.fail("删除失败");
    }

}
