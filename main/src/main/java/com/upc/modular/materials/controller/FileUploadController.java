package com.upc.modular.materials.controller;

import com.upc.common.responseparam.R;
import com.upc.modular.materials.service.IFileUploadService; // <-- 引入新的Service接口
import com.upc.modular.materials.controller.param.dto.FileUploadResultDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor; // <-- 用于构造器注入
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/file")
@Api(tags = "业务文件上传")
@RequiredArgsConstructor // <-- Lombok注解，自动生成final字段的构造函数
public class FileUploadController {

/*    image: 单张图片
    imageSet: 图集（多张图片）
    video: 视频
    audio: 音频
3DModel: 3D模型
    link: 外部链接
    pdf: PDF文档
    word: Word文档
    excel: Excel表格
    ppt: PPT演示文稿
    other: 其他类型文件*/
    // 使用final和构造器注入，这是推荐的最佳实践
    private final IFileUploadService fileUploadService;

    @PostMapping("/upload-material")
    @ApiOperation(value = "上传教学素材文件（携带业务参数）")
    public R<FileUploadResultDTO> uploadMaterialFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("isPublic") Boolean isPublic,
            @RequestParam("type") String type) {

        // 1. 调用Service层处理业务逻辑
        FileUploadResultDTO result = fileUploadService.uploadMaterialFiles(files, isPublic, type);

        // 2. 将Service返回的结果包装成统一的响应格式
        return R.ok(result);
    }
}
