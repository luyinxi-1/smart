package com.upc.modular.materials.controller;


import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
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
    public R<String> insertMaterials(@RequestParam(value = "file") MultipartFile multipartFile, @ModelAttribute TeachingMaterials teachingMaterials) {
        try {
            String fileName = teachingMaterialsService.insertMaterials(multipartFile, teachingMaterials);
            return R.ok(fileName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("上传失败");
        }
    }

    @ApiOperation(value = "下载教学素材")
    @GetMapping("/download-materials")
    public void downloadMaterials(@RequestParam String fileName, @RequestParam Long textbookId, @RequestParam String action, HttpServletResponse response) {
        try {
            teachingMaterialsService.downloadMaterials(fileName, textbookId, action, response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "下载文件失败");
        }
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
        try{
            String url = teachingMaterialsService.getLinkMaterials(fileName, textbookId);
            return R.ok(url);
        } catch (BusinessException e) {
            throw e;
        }
    }

    @ApiOperation(value = "添加图集素材")
    @PostMapping("/insert-picture-materials")
    public R<String> insertPictureMaterials(@RequestParam(value = "file") List<MultipartFile> files, @ModelAttribute TeachingMaterials teachingMaterials) {
        try {
            String fileName = teachingMaterialsService.insertPictureMaterials(files, teachingMaterials);
            return R.ok(fileName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("上传失败");
        }
    }

    @ApiOperation(value = "查看图集素材")
    @GetMapping("/get-one-picture-materials")
    public void getOnePictureMaterials(@RequestParam String fileName, @RequestParam Long textbookId, String action, HttpServletResponse response) {
        try {
            teachingMaterialsService.getOnePictureMaterials(fileName, textbookId, action, response);
        } catch (BusinessException e) {
            throw e;
        }
    }
}
