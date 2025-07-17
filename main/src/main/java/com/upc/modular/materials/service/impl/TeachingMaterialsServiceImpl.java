package com.upc.modular.materials.service.impl;

import com.upc.common.utils.FileManageUtil;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.mapper.TeachingMaterialsMapper;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

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
            return FileManageUtil.uploadFile(multipartFile, Paths.get("teaching_materials" + UserUtils.get().getId().toString()));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("上传失败");
        }
    }
}
