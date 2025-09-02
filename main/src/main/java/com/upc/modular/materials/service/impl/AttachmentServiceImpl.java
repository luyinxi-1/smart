package com.upc.modular.materials.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.common.utils.FileManageUtil;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.controller.param.vo.AttachmentListReturnParam;
import com.upc.modular.materials.entity.Attachment;
import com.upc.modular.materials.mapper.AttachmentMapper;
import com.upc.modular.materials.service.IAttachmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.upc.common.utils.FileManageUtil.createFileName;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author mjh
 * @since 2025-09-02
 */
@Service
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper, Attachment> implements IAttachmentService {

    @Override
    public String insertAttachment(MultipartFile file, String type, String objectType, Long objectId) {
        if (ObjectUtils.isEmpty(file) || ObjectUtils.isEmpty(type) || ObjectUtils.isEmpty(objectType) || ObjectUtils.isEmpty(objectId))
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数不能为空");
        String fileName = createFileName(file);
        Path folderPath = Paths.get("upload", "public", "attachment", objectType, objectId.toString());
        String filePath = FileManageUtil.uploadFile(file, folderPath, fileName);
        Double fileSize = Math.round(file.getSize() / (1024.0 * 1024.0) * 100) / 100.0;
        Attachment attachment = new Attachment()
                .setFileName(fileName)
                .setFileType(type)
                .setFileSize(fileSize)
                .setFilePath(filePath)
                .setStatus(0)
                .setObjectType(objectType)
                .setObjectId(objectId);
        if (this.save(attachment))
            return attachment.getFilePath();
        throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，文件上传失败");
    }

    @Override
    public boolean remove(Long id) {
        Attachment attachment = this.getById(id);
        if (attachment == null)
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，文件不存在");
        return FileManageUtil.deleteFile(attachment.getFilePath()) && this.removeById(id);
    }

    @Override
    public List<AttachmentListReturnParam> getAttachmentList(String objectType, Long objectId) {
        if (ObjectUtils.isEmpty(objectType) || ObjectUtils.isEmpty(objectId))
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数错误");
        List<Attachment> attachmentList = this.list(
                new LambdaQueryWrapper<Attachment>()
                        .eq(Attachment::getObjectType, objectType)
                        .eq(Attachment::getObjectId, objectId));
        List<AttachmentListReturnParam> returnParamList = attachmentList.stream()
                .map(attachment -> new AttachmentListReturnParam()
                        .setId(attachment.getId())
                        .setType(attachment.getFileType())
                        .setFilePath(attachment.getFilePath()))
                .collect(Collectors.toList());
        return returnParamList;
    }

}
