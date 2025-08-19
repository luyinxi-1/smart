package com.upc.modular.materials.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.utils.FileManageUtil;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.mapper.TeachingMaterialsMapper;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author mjh
 * @since 2025-07-17
 */
@Service
public class TeachingMaterialsServiceImpl extends ServiceImpl<TeachingMaterialsMapper, TeachingMaterials> implements ITeachingMaterialsService {

    /**
     * 添加教学素材
     *
     * @param multipartFile
     * @param teachingMaterials
     * @return
     */
    @Override
    public String insertMaterials(MultipartFile multipartFile, TeachingMaterials teachingMaterials) {
        try {
            // 路径：upload/teaching_materials/用户id/文件类型/yyyyMMdd/文件名
            String filePath = FileManageUtil.uploadFile(multipartFile,
                    Paths.get("teaching_materials",
                            UserUtils.get().getId().toString(),
                            teachingMaterials.getType(), FileManageUtil.yyyyMMddStr()));
            if (ObjectUtils.isEmpty(filePath))
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，上传失败");

            teachingMaterials.setId(null);
            teachingMaterials.setFilePath(filePath);
            teachingMaterials.setAuthorId(UserUtils.get().getId());
            teachingMaterials.setFileSize(Math.round(multipartFile.getSize() / (1024.0 * 1024.0) * 100) / 100.0);
            teachingMaterials.setAddDatetime(LocalDateTime.now());
            if (!this.save(teachingMaterials)) {
                FileManageUtil.deleteFile(filePath);
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，上传失败");
            }

            // 上传成功，返回文件路径
            return filePath;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，上传失败");
        }
    }

    /**
     * 下载教学素材
     *
     * @param fileId   文件id
     * @param fileName 文件名
     * @param response
     */
    @Override
    public void downloadMaterials(Long fileId, String fileName, HttpServletResponse response) {
        try {
            if (ObjectUtils.isEmpty(fileId) && ObjectUtils.isEmpty(fileName)) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数不能为空");
            }

            // 获取并验证教学材料
            TeachingMaterials materials = getAndValidateMaterials(fileId, fileName);

            // 权限验证
//            Long userId = UserUtils.get().getId();
//            if (!materials.getAuthorId().equals(userId)) {
//                throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "，没有权限下载此文件");
//            }

            // 执行文件下载
            performFileDownload(materials, response);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("下载文件时发生未知错误", e);
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，下载文件失败");
        }
    }

    /**
     * 获取并验证教学材料
     */
    private TeachingMaterials getAndValidateMaterials(Long fileId, String fileName) {
        TeachingMaterials materials = null;

        if (ObjectUtils.isNotEmpty(fileId)) {
            materials = this.getById(fileId);
            if (ObjectUtils.isEmpty(materials)) {
                throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST, "，文件不存在");
            }
        } else {
            List<TeachingMaterials> materialsList = this.list(
                    new LambdaQueryWrapper<TeachingMaterials>()
                            .eq(TeachingMaterials::getName, fileName));

            if (ObjectUtils.isEmpty(materialsList)) {
                throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST, "，文件不存在");
            }
            if (materialsList.size() > 1) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，存在多个同名文件");
            }
            materials = materialsList.get(0);
        }

        return materials;
    }

    /**
     * 执行文件下载
     */
    private void performFileDownload(TeachingMaterials materials, HttpServletResponse response) {
        // 构建文件路径
        Path userHome = Paths.get(System.getProperty("user.dir"));
        Path filePath = userHome.resolve(materials.getFilePath());
        File file = filePath.toFile();

        if (!file.exists())
            throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST, "文件不存在");

        // 构造下载文件名
        String downloadFileName = buildDownloadFileName(materials);

        // 设置响应头
        setupResponseHeaders(response, file, downloadFileName);

        // 执行文件传输
        transferFile(file, response, materials.getName());
    }

    /**
     * 构造下载文件名
     * 如果文件名包含扩展名，则使用原始扩展名，否则使用文件名
     */
    private String buildDownloadFileName(TeachingMaterials materials) {
        String filePath = materials.getFilePath();
        String originalFileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        if (originalFileName.contains(".")) {
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            return materials.getName() + fileExtension;
        } else {
            return materials.getName();
        }
    }

    /**
     * 设置响应头
     */
    private void setupResponseHeaders(HttpServletResponse response, File file, String fileName) {
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        response.setContentLengthLong(file.length());

        try {
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            String contentDisposition = "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName;
            response.setHeader("Content-Disposition", contentDisposition);
        } catch (UnsupportedEncodingException e) {
            // 文件名编码失败，使用默认文件名
            response.setHeader("Content-Disposition", "attachment; filename=\"download.file\"");
        }
    }

    /**
     * 执行文件传输
     */
    private void transferFile(File file, HttpServletResponse response, String materialName) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {

            byte[] buffer = new byte[1024 * 8]; // 使用更大的缓冲区提高性能
            int bytesRead;

            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            bos.flush();

        } catch (IOException e) {
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，文件下载失败");
        }
    }
}
