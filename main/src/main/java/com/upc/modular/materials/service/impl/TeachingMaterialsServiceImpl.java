package com.upc.modular.materials.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.utils.FileManageUtil;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.entity.MaterialsTextbookMapping;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.mapper.TeachingMaterialsMapper;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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

    @Autowired
    private TeachingMaterialsMapper teachingMaterialsMapper;

    @Autowired
    private MaterialsTextbookMappingServiceImpl materialsTextbookMappingService;

    /**
     * 添加教学素材
     *
     * @param file
     * @param teachingMaterials
     * @return
     */
    @Override
    public String insertMaterials(MultipartFile file, TeachingMaterials teachingMaterials) {
        try {
            // 查看该作者是否有重名素材
            if (teachingMaterialsMapper.selectOne(new LambdaQueryWrapper<TeachingMaterials>()
                    .eq(TeachingMaterials::getName, teachingMaterials.getName())
                    .eq(TeachingMaterials::getAuthorId, UserUtils.get().getId())) != null)
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，该命名素材已存在");
            // 公共素材路径：upload/teaching_materials/public/文件类型/yyyyMMdd/文件名
            // 私有素材路径：upload/teaching_materials/private/用户id/文件类型/yyyyMMdd/文件名
            Path folderPath;
            if (teachingMaterials.getIsPublic())
                folderPath = Paths.get("upload", "teaching_materials", "public",
                        teachingMaterials.getType(), FileManageUtil.yyyyMMddStr());
            else
                folderPath = Paths.get("upload", "teaching_materials", "private",
                        UserUtils.get().getId().toString(),
                        teachingMaterials.getType(), FileManageUtil.yyyyMMddStr());

            String fileName = FileManageUtil.createFileName(file);

            String filePath = FileManageUtil.uploadFile(file, folderPath, fileName);
            if (ObjectUtils.isEmpty(filePath))
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，上传失败");

            teachingMaterials.setId(null);
            teachingMaterials.setAuthorId(UserUtils.get().getId());
            teachingMaterials.setFileName(fileName);
            teachingMaterials.setFileSize(Math.round(file.getSize() / (1024.0 * 1024.0) * 100) / 100.0);
            teachingMaterials.setFilePath(filePath);
            teachingMaterials.setAddDatetime(LocalDateTime.now());
            if (!this.save(teachingMaterials)) {
                FileManageUtil.deleteFile(filePath);
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，上传失败");
            }

            // 上传成功，返回文件名
            return fileName;
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
     * @param fileName   文件名
     * @param textbookId 绑定的教材id
     * @param action     下载方式
     * @param response
     */
    @Override
    public void downloadMaterials(String fileName, Long textbookId, String action, HttpServletResponse response) {
        try {
            if (ObjectUtils.isEmpty(fileName))
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数不能为空");

            // 获取并验证教学材料
            TeachingMaterials materials = teachingMaterialsMapper.selectOne(new LambdaQueryWrapper<TeachingMaterials>().eq(TeachingMaterials::getFileName, fileName));
            if (ObjectUtils.isEmpty(materials))
                throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST, "，文件不存在");

            // 验证权限，如果是从教材中下载，则增加下载查看次数用于数据统计
            if (!validateDownloadPermission(materials, textbookId, action))
                throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "，没有下载权限");

            // 执行文件下载
            performFileDownload(materials, action, response);

        } catch (BusinessException e) {
            response.reset();
            throw e;
        } catch (Exception e) {
            log.error("下载文件时发生未知错误", e);
            response.reset();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，下载文件失败");
        }
    }

    /**
     * 添加链接素材
     *
     * @param teachingMaterials
     * @return
     */
    @Override
    public String insertLinkMaterials(TeachingMaterials teachingMaterials) {
        // 查看该作者是否有重名素材
        if (teachingMaterialsMapper.selectOne(new LambdaQueryWrapper<TeachingMaterials>()
                .eq(TeachingMaterials::getName, teachingMaterials.getName())
                .eq(TeachingMaterials::getAuthorId, UserUtils.get().getId())) != null)
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，该命名素材已存在");
        if (teachingMaterials.getType().equals("链接")) {
            String urlName = UUID.randomUUID().toString();
            teachingMaterials.setAuthorId(UserUtils.get().getId());
            teachingMaterials.setFileName(urlName);
            boolean save = this.save(teachingMaterials);
            if (save)
                return teachingMaterials.getFileName();
        }
        return null;
    }

    /**
     * 获取链接素材
     *
     * @param fileName
     * @param textbookId
     * @return
     */
    @Override
    public String getLinkMaterials(String fileName, Long textbookId) {
        try {
            if (ObjectUtils.isEmpty(fileName))
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数不能为空");

            // 获取并验证教学材料
            TeachingMaterials materials = teachingMaterialsMapper.selectOne(new LambdaQueryWrapper<TeachingMaterials>().eq(TeachingMaterials::getFileName, fileName));
            if (ObjectUtils.isEmpty(materials))
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，链接不存在");

            // 验证权限，如果是从教材中下载，则增加下载查看次数用于数据统计
            if (!validateDownloadPermission(materials, textbookId, "view"))
                throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "，没有查看权限");

            return materials.getFilePath();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，链接查看失败");
        }
    }

    /**
     * 添加图集素材
     *
     * @param files
     * @param teachingMaterials
     * @return
     */
    @Override
    public String insertPictureMaterials(List<MultipartFile> files, TeachingMaterials teachingMaterials) {
        try {
            // 查看该作者是否有重名素材
            if (teachingMaterialsMapper.selectOne(new LambdaQueryWrapper<TeachingMaterials>()
                    .eq(TeachingMaterials::getName, teachingMaterials.getName())
                    .eq(TeachingMaterials::getAuthorId, UserUtils.get().getId())) != null)
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，该命名素材已存在");
            // 公共素材路径：upload/teaching_materials/public/pictures/yyyyMMdd/图集uuid/图片
            // 私有素材路径：upload/teaching_materials/private/用户id/pictures/yyyyMMdd/图集uuid/图片
            Path folderPath;
            String picturesName = UUID.randomUUID().toString().substring(0, 12);
            if (teachingMaterials.getIsPublic())
                folderPath = Paths.get("upload", "teaching_materials", "public",
                        "图集", FileManageUtil.yyyyMMddStr(), picturesName);
            else
                folderPath = Paths.get("upload", "teaching_materials", "private",
                        UserUtils.get().getId().toString(),
                        "图集", FileManageUtil.yyyyMMddStr(), picturesName);
            List<String> listFileName = new ArrayList<>();
            List<String> listPath = new ArrayList<>();
            Long filesSize = 0L;
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String fileName = FileManageUtil.createFileName(file, String.valueOf(i + 1));

                String filePath = FileManageUtil.uploadFile(file, folderPath, fileName);
                if (ObjectUtils.isEmpty(filePath))
                    throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，上传失败");
                listFileName.add(Paths.get(picturesName, fileName).toString());
                listPath.add(filePath);
                filesSize += file.getSize();
            }
            teachingMaterials.setId(null);
            teachingMaterials.setAuthorId(UserUtils.get().getId());
            teachingMaterials.setFileName(listFileName.toString());
            teachingMaterials.setFileSize(Math.round(filesSize / (1024.0 * 1024.0) * 100) / 100.0);
            teachingMaterials.setFilePath(listPath.toString());
            teachingMaterials.setAddDatetime(LocalDateTime.now());
            if (!this.save(teachingMaterials))
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，上传失败");

            // 上传成功，返回文件名
            return teachingMaterials.getFileName();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，上传失败");
        }
    }

    /**
     * 获取图集素材
     *
     * @param fileName
     * @param textbookId
     * @param action
     */
    @Override
    public void getOnePictureMaterials(String fileName, Long textbookId, String action, HttpServletResponse response) {
        try {
            if (ObjectUtils.isEmpty(fileName))
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数不能为空");

            // 获取并验证教学材料
            TeachingMaterials materials = teachingMaterialsMapper.selectOne(new LambdaQueryWrapper<TeachingMaterials>().like(TeachingMaterials::getFileName, fileName));
            if (ObjectUtils.isEmpty(materials))
                throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST, "，文件不存在");

            fileName = fileName.replace("\\\\", "\\");
            List<String> listFileName = Arrays.asList(
                    materials.getFileName()
                            .substring(1, materials.getFileName().length() - 1)
                            .split(",\\s*"));
            // 获取匹配的下标
            int index = listFileName.indexOf(fileName);
            List<String> listPath = Arrays.asList(
                    materials.getFilePath()
                            .substring(1, materials.getFilePath().length() - 1)
                            .split(",\\s*"));
            String filePath = listPath.get(index);
            materials.setFilePath(filePath);
            materials.setName(materials.getName() + "_" + (index + 1));
            // 验证权限，如果是从教材中下载，则增加下载查看次数用于数据统计
            if (!validateDownloadPermission(materials, textbookId, action))
                throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "，没有下载权限");

            // 执行文件下载
            performFileDownload(materials, action, response);

        } catch (BusinessException e) {
            response.reset();
            throw e;
        } catch (Exception e) {
            log.error("下载文件时发生未知错误", e);
            response.reset();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，下载文件失败");
        }
    }

    /**
     * 验证下载权限，如果从教材中下载，则增加下载查看次数用于数据统计
     *
     * @param materials  教学材料对象
     * @param textbookId 验证的教材ID
     */
    private Boolean validateDownloadPermission(TeachingMaterials materials, Long textbookId, String action) {
        // 如果是公开素材，则无需验证权限
        if (materials.getIsPublic())
            return true;

        // 如果是作者本人，则拥有下载权限
        Long currentUserId = UserUtils.get().getId();
        if (materials.getAuthorId().equals(currentUserId))
            return true;

        // 如果不是作者，则必须提供有效的 textbookId 才能继续验证
        if (textbookId == null)
            return false;

        // 检查是否有权限通过该教材访问此文件
        List<MaterialsTextbookMapping> textbookMappingList = materialsTextbookMappingService.list(
                new LambdaQueryWrapper<MaterialsTextbookMapping>()
                        .eq(MaterialsTextbookMapping::getMaterialId, materials.getId()));

        int index = textbookMappingList.indexOf(
                textbookMappingList.stream()
                        .filter(mapping -> mapping.getTextbookId().equals(textbookId))
                        .findFirst()
                        .orElse(null) // 如果没找到，返回 null，indexOf(null) 会查找 null 元素
        );
        if (index != -1) {
            MaterialsTextbookMapping mapping = textbookMappingList.get(index);
            if (action.equals("view")) {
                mapping.setViewCount(mapping.getViewCount() + 1);
            } else if (action.equals("download")) {
                mapping.setDownloadCount(mapping.getDownloadCount() + 1);
            } else {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数错误");
            }
            materialsTextbookMappingService.updateById(mapping);
            return true;
        }
        return false;
    }

    /**
     * 执行文件下载
     */
    private void performFileDownload(TeachingMaterials materials, String action, HttpServletResponse response) {
        File file = new File(materials.getFilePath());
        if (!file.exists())
            throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST, "，文件不存在");

        // 构造下载文件名
        String downloadFileName = buildDownloadFileName(materials);

        // 设置响应头
        setupResponseHeaders(response, file, downloadFileName, action);

        // 执行文件传输
        FileManageUtil.transferFile(file, response);
    }

    /**
     * 构造下载文件名
     * 如果文件名包含扩展名，则使用原始扩展名，否则使用文件名
     */
    private String buildDownloadFileName(TeachingMaterials materials) {
        String filePath = materials.getFilePath();
        String originalFileName = "";
        if (filePath.startsWith("upload/"))         // linux
            originalFileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        else if (filePath.startsWith("upload\\"))   // windows
            originalFileName = filePath.substring(filePath.lastIndexOf("\\") + 1);

        if (originalFileName.contains(".")) {
            String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
            return materials.getName() + suffix;
        } else {
            return materials.getName();
        }
    }

    /**
     * 设置响应头
     */
    private void setupResponseHeaders(HttpServletResponse response, File file, String fileName, String action) {
        try {
            String mimeType = Files.probeContentType(file.toPath());
            response.setContentType(mimeType);
        } catch (Exception e) {
            log.error("获取文件类型失败", e);
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，获取文件类型失败");
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentLengthLong(file.length());

        try {
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            String contentDisposition = "filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName;
            if (action.equals("download"))
                contentDisposition = "attachment;" + contentDisposition;
            else if (action.equals("view"))
                contentDisposition = "inline;" + contentDisposition;
            else throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数 action 错误");
            response.setHeader("Content-Disposition", contentDisposition);
        } catch (UnsupportedEncodingException e) {
            // 文件名编码失败，使用默认文件名
            response.setHeader("Content-Disposition", "attachment; filename=\"download.file\"");
        }
    }
}
