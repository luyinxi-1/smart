package com.upc.modular.materials.controller;

import com.upc.common.responseparam.R;
import com.upc.modular.materials.service.IFileUploadService;
import com.upc.modular.materials.controller.param.dto.FileUploadResultDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/file")
@Api(tags = "业务文件上传")
@RequiredArgsConstructor
public class FileUploadController {

    private final IFileUploadService fileUploadService;

    @PostMapping("/upload-material")
    @ApiOperation(value = "上传教学素材文件")
    public R<String> uploadMaterialFile(
            @RequestParam("file") MultipartFile file,
            @ApiParam(value = "素材文件类型。例如: image, video, audio, pdf, word, excel,ppt,H5,simulation,other", required = true)
            @RequestParam("type") String type) {

        String filePath = fileUploadService.uploadMaterialFile(file, type);
        return R.ok(filePath);
    }
}