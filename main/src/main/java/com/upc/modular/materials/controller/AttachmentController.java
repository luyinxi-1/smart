package com.upc.modular.materials.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.materials.controller.param.vo.AttachmentListReturnParam;
import com.upc.modular.materials.service.IAttachmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author mjh
 * @since 2025-09-02
 */
@Api(tags = "附件管理")
@RestController
@RequestMapping("/attachment")
public class AttachmentController {

    @Autowired
    private IAttachmentService attachmentService;


    @ApiOperation(value = "获取附件列表")
    @PostMapping("/get-attachment-list")
    public R<List<AttachmentListReturnParam>> getFileList(@RequestParam String objectType, @RequestParam Long objectId) {
        List<AttachmentListReturnParam> list = attachmentService.getAttachmentList(objectType, objectId);
        return R.ok(list);
    }

    @ApiOperation(value = "添加附件")
    @PostMapping("/insert-file")
    public R<String> insertFile(@RequestParam(value = "file") MultipartFile file, @RequestParam String type, @RequestParam String objectType, @RequestParam Long objectId) {
        String filePath = attachmentService.insertAttachment(file, type, objectType, objectId);
        return R.ok(filePath);
    }

    @ApiOperation(value = "删除附件")
    @PostMapping("/delete-file")
    public R<Boolean> deleteFile(@RequestParam Long id) {
        return R.ok(attachmentService.remove(id));
    }

}
