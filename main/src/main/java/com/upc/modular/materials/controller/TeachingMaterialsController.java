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
            String path = teachingMaterialsService.insertMaterials(multipartFile, teachingMaterials);
            return R.ok(path);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("上传失败，请联系管理员");
        }
    }

    @ApiOperation(value = "下载教学素材（fileId和fileName使用一个，但都需要传参，有鉴权：仅上传者能下载）")
    @GetMapping("/download-materials")
    public void downloadMaterials(@RequestParam("fileId") Long fileId, @RequestParam("fileName") String fileName, HttpServletResponse response) {
        try {
            teachingMaterialsService.downloadMaterials(fileId, fileName, response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "下载文件失败");
        }

    }
}
