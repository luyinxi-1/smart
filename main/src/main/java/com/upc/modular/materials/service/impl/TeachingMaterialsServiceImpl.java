package com.upc.modular.materials.service.impl;

import com.upc.common.utils.FileManageUtil;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.mapper.TeachingMaterialsMapper;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mjh
 * @since 2025-07-17
 */
@Service
public class TeachingMaterialsServiceImpl extends ServiceImpl<TeachingMaterialsMapper, TeachingMaterials> implements ITeachingMaterialsService {

    @Override
    public String insertMaterials(MultipartFile multipartFile, TeachingMaterials teachingMaterials){
        try {
            String filePath = FileManageUtil.uploadFile(multipartFile, Paths.get("teaching_materials", UserUtils.get().getId().toString(), teachingMaterials.getType(), FileManageUtil.yyyyMMddStr()));
            if(filePath != null) {
                teachingMaterials.setId(null);
                teachingMaterials.setFilePath(filePath);
                teachingMaterials.setAuthorId(UserUtils.get().getId());
                teachingMaterials.setFileSize(Math.round(multipartFile.getSize()/(1024.0*1024.0) * 100)/100.0);
                teachingMaterials.setAddDatetime(LocalDateTime.now());
                if(this.save(teachingMaterials)) {
                    return filePath;
                }
                else {
                    FileManageUtil.deleteFile(filePath);
                    throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，上传失败");
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("上传失败");
        }
        throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "上传失败");
    }
}
