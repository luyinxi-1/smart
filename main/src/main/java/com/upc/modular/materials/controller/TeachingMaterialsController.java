package com.upc.modular.materials.controller;


import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.institution.entity.Institution;
import com.upc.modular.materials.controller.param.dto.TeachingMaterialsPageSearchDto;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsReturnVo;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import com.upc.modular.student.controller.param.dto.StudentPageSearchDto;
import com.upc.modular.student.controller.param.vo.StudentReturnVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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
    @PostMapping("/insert-file-materials")
    public R<String> insertFileMaterials(@RequestParam(value = "file") MultipartFile multipartFile, @ModelAttribute TeachingMaterials teachingMaterials) {
        String fileName = teachingMaterialsService.insertFileMaterials(multipartFile, teachingMaterials);
        return R.ok(fileName);
    }

    @ApiOperation(value = "下载教学素材")
    @GetMapping("/download-file-materials")
    public void downloadFileMaterials(@RequestParam String fileName, @RequestParam Long textbookId, @RequestParam String action, HttpServletResponse response) {
        teachingMaterialsService.getFileMaterials(fileName, textbookId, action, response);
    }

    @ApiOperation(value = "添加链接素材（链接填在filePath字段）")
    @PostMapping("/insert-link-materials")
    public R<String> insertLinkMaterials(@ModelAttribute TeachingMaterials teachingMaterials) {
        String urlName = teachingMaterialsService.insertLinkMaterials(teachingMaterials);
        if (urlName == null)
            return R.fail("添加失败");
        return R.ok(urlName);
    }

    @ApiOperation(value = "查看链接素材")
    @GetMapping("/get-link-materials")
    public R<String> getLinkMaterials(@RequestParam String fileName, @RequestParam Long textbookId) {
        String url = teachingMaterialsService.getLinkMaterials(fileName, textbookId);
        return R.ok(url);
    }

    @ApiOperation(value = "添加图集素材")
    @PostMapping("/insert-picture-materials")
    public R<String> insertPictureMaterials(@RequestParam(value = "file") List<MultipartFile> files, @ModelAttribute TeachingMaterials teachingMaterials) {
        String fileName = teachingMaterialsService.insertPictureMaterials(files, teachingMaterials);
        return R.ok(fileName);
    }

    @ApiOperation(value = "查看图集素材")
    @GetMapping("/get-one-picture-materials")
    public void getOnePictureMaterials(@RequestParam String fileName, @RequestParam Long textbookId, String action, HttpServletResponse response) {
        teachingMaterialsService.getOnePictureMaterials(fileName, textbookId, action, response);
    }

    @ApiOperation(value = "分页查询教学素材")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<TeachingMaterialsReturnVo>> getPage(@RequestBody TeachingMaterialsPageSearchDto param) {
        Page<TeachingMaterialsReturnVo> page = teachingMaterialsService.getPage(param);
        PageBaseReturnParam<TeachingMaterialsReturnVo> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }
    // 查看教学素材
    @ApiOperation(value = "查看教学素材(学生查看时用到textbookId)")
    @GetMapping("/get-teaching-materials")
    public R<TeachingMaterialsReturnVo> get(@RequestParam Long id, @RequestParam Long textbookId) {
        TeachingMaterialsReturnVo teachingMaterials = teachingMaterialsService.getTeachingMaterials(id, textbookId);
        return R.ok(teachingMaterials);
    }
    @ApiOperation(value = "修改教学素材信息")
    @PostMapping("/updateTeachingMaterialsById")
    public R updateInstitutionById(@RequestBody TeachingMaterials teachingmaterials) {
        teachingMaterialsService.updateTeachingMaterialsById(teachingmaterials);
        return R.commonReturn(200, "修改成功", "");
    }
    @ApiOperation(value = "删除教学素材信息")
    @PostMapping("/deleteTeachingMaterialsByIds")
    public R deleteTeachingMaterialsByIds (@RequestBody List<Long> ids) {
        teachingMaterialsService.deleteTeachingMaterialsByIds(ids);
        return R.commonReturn(200, "删除成功", "");
    }
}
