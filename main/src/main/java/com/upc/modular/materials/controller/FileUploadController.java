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
    public R<FileUploadResultDTO> uploadMaterialFiles(
            @RequestParam("files") List<MultipartFile> files,
            @ApiParam(value = "素材文件类型。例如: image,imageSet, video, audio,pdf,3DModel ,link,word ,pdf,excel,other", required = true)
            @RequestParam("type") String type) {

        FileUploadResultDTO result = fileUploadService.uploadMaterialFiles(files, type);
        return R.ok(result);
    }
}