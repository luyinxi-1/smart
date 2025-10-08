package com.upc.modular.materials.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.modular.materials.controller.param.vo.AttachmentListReturnParam;
import com.upc.modular.materials.entity.Attachment;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mjh
 * @since 2025-09-02
 */
public interface IAttachmentService extends IService<Attachment> {

    String insertAttachment(MultipartFile file, String type, String objectType, Long objectId);

    boolean remove(Long id);

    List<AttachmentListReturnParam> getAttachmentList(String objectType, Long objectId);
}