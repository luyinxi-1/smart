package com.upc.modular.materials.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.utils.FileManageUtil;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.controller.param.dto.TeachingMaterialsPageSearchDto;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsReturnVo;
import com.upc.modular.materials.entity.MaterialsTextbookMapping;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.mapper.TeachingMaterialsMapper;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.service.impl.TeacherServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.upc.utils.CreatePage.createPage;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author mjh
 * @since 2025-07-17
 */
@Slf4j
@Service
public class TeachingMaterialsServiceImpl extends ServiceImpl<TeachingMaterialsMapper, TeachingMaterials> implements ITeachingMaterialsService {

    @Autowired
    private TeachingMaterialsMapper teachingMaterialsMapper;

    @Autowired
    private MaterialsTextbookMappingServiceImpl materialsTextbookMappingService;

    @Autowired
    private TeacherServiceImpl teacherService;

    /**
     * 添加文件素材
     *
     * @param file
     * @param teachingMaterials
     * @return
     */
    @Override
    public String insertFileMaterials(MultipartFile file, TeachingMaterials teachingMaterials) {
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
            if (!this.save(teachingMaterials)) {
                try {
                    Files.deleteIfExists(Paths.get(filePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
     * 获取文件素材
     *
     * @param fileName   文件名
     * @param textbookId 绑定的教材id
     * @param action     下载方式
     * @param response
     */
    @Override
    public void getFileMaterials(String fileName, Long textbookId, String action, HttpServletResponse response) {
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
            e.printStackTrace();
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
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
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
            if (!this.save(teachingMaterials))
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，上传失败");

            // 上传成功，返回文件名
            return teachingMaterials.getFileName();
        } catch (BusinessException e) {
            e.printStackTrace();
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
            e.printStackTrace();
            response.reset();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
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
        // 如果是公开素材或是管理员用户，则无需验证权限
        if (materials.getIsPublic() || UserUtils.get().getUserType() == 0)
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
        Path filePath = Paths.get(materials.getFilePath());
        if (!Files.exists(filePath))
            throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST, "，文件不存在");

        // 构造下载文件名
        String downloadFileName = buildDownloadFileName(materials);

        // 设置响应头
        setupResponseHeaders(response, filePath, downloadFileName, action);

        // 执行文件传输
        FileManageUtil.transferFile(filePath, response);
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
    private void setupResponseHeaders(HttpServletResponse response, Path filePath, String fileName, String action) {
        try {
            String mimeType = Files.probeContentType(filePath);
            response.setContentType(mimeType);
        } catch (Exception e) {
            log.error("获取文件类型失败", e);
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，获取文件类型失败");
        }

        response.setCharacterEncoding("UTF-8");
        try {
            response.setContentLengthLong(Files.size(filePath));
        } catch (IOException e) {
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，获取文件大小失败");
        }

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

    @Override
    public Page<TeachingMaterialsReturnVo> getPage(TeachingMaterialsPageSearchDto param) {

        MyLambdaQueryWrapper<TeachingMaterials> queryWrapper = new MyLambdaQueryWrapper<>();

        // 用户类型（0管理员、1学生、2教师）
        Integer userType = UserUtils.get().getUserType();
        if (userType == 0) {
            queryWrapper.eq(TeachingMaterials::getAuthorId, param.getAuthorId())
                    .like(TeachingMaterials::getName, param.getName())
                    .eq(TeachingMaterials::getType, param.getType())
                    .eq(TeachingMaterials::getIsPublic, param.getIsPublic());
        } else if (userType == 1)
            throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS);
        else if (userType == 2) {
            queryWrapper.like(TeachingMaterials::getName, param.getName())
                    .eq(TeachingMaterials::getType, param.getType());
            if (ObjectUtils.isNotEmpty(param.getIsPublic())) {
                if (param.getIsPublic())
                    queryWrapper.eq(TeachingMaterials::getIsPublic, param.getIsPublic());
                else queryWrapper.eq(TeachingMaterials::getAuthorId, UserUtils.get().getId());
            }
        }

        List<TeachingMaterials> materialsList = this.list(queryWrapper);

        List<Long> authorIdList = materialsList.stream().map(TeachingMaterials::getAuthorId).collect(Collectors.toList());
        Map<Long, String> teacherIdNameMap = teacherService.list(
                        new LambdaQueryWrapper<Teacher>().in(Teacher::getId, authorIdList))
                .stream().collect(
                        Collectors.toMap(Teacher::getId, Teacher::getName));

        List<TeachingMaterialsReturnVo> pageRecordsVO = materialsList.stream()
                .map(materials -> {
                    TeachingMaterialsReturnVo temp = new TeachingMaterialsReturnVo();
                    BeanUtils.copyProperties(materials, temp);
                    temp.setAuthorName(teacherIdNameMap.get(materials.getAuthorId()));
                    return temp;
                })
                .sorted(Comparator.comparing(TeachingMaterialsReturnVo::getAddDatetime).reversed())
                .collect(Collectors.toList());

        Page<TeachingMaterialsReturnVo> resultPage = createPage(pageRecordsVO, param.getCurrent(), param.getSize());

        return resultPage;
    }

    @Override
    public TeachingMaterialsReturnVo getTeachingMaterials(Long id, Long textbookId) {
        if (id == null)
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数 id 不能为空");
        TeachingMaterialsReturnVo materialsReturnVo = new TeachingMaterialsReturnVo();
        TeachingMaterials materials = this.getById(id);
        if (ObjectUtils.isEmpty(materials))
            throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST);
        if (!materials.getIsPublic() && !materials.getAuthorId().equals(UserUtils.get().getId()) && UserUtils.get().getUserType() != 0) {
            if (textbookId == null)
                throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "，没有权限查看此文件");
            List<MaterialsTextbookMapping> textbookMappingList = materialsTextbookMappingService.list(
                    new LambdaQueryWrapper<MaterialsTextbookMapping>()
                            .eq(MaterialsTextbookMapping::getMaterialId, id)
                            .eq(MaterialsTextbookMapping::getTextbookId, textbookId));
            if (textbookMappingList.size() == 0)
                throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "，没有权限查看此文件");
        }
        BeanUtils.copyProperties(materials, materialsReturnVo);
        Teacher teacher = teacherService.getById(materials.getAuthorId());
        if (teacher != null && ObjectUtils.isNotEmpty(teacher.getName()))
            materialsReturnVo.setAuthorName(teacher.getName());

        return materialsReturnVo;
    }

    @Override
    public void updateTeachingMaterialsById(TeachingMaterials teachingmaterials) {
        if (teachingmaterials == null || teachingmaterials.getId() == null || teachingmaterials.getId() == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        TeachingMaterials oldData = this.getById(teachingmaterials.getId());
        // 判断isPublic是否发生变化
        boolean oldPublic = oldData.getIsPublic();
        boolean newIsPublic = teachingmaterials.getIsPublic();
        if (oldPublic != newIsPublic) {
            // 发生改变则移动文件
            String oldPath = oldData.getFilePath();
            String baseDir = newIsPublic ? "upload/teaching_materials/public/" : "upload/teaching_materials/private/";
            String newPath = baseDir + extractRelativePath(oldPath);
            //log.info("移动文件：{} -> {}", oldPath, newPath);
            boolean moved = FileManageUtil.moveFile(oldPath, newPath);
            if (!moved) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "文件移动失败");
            }
        }
        this.updateById(teachingmaterials);
    }

    /**
     * 提取文件路径中的文件名
     *
     * @param filePath 文件完整路径
     * @return 文件名
     */
    private String extractRelativePath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        // 统一路径分隔符（把 \ 换成 /）
        String normalizedPath = filePath.replace('\\', '/');

        // 定义前缀
        String publicPrefix = "upload/teaching_materials/public/";
        String privatePrefix = "upload/teaching_materials/private/";

        String relativePath;
        if (normalizedPath.startsWith(publicPrefix)) {
            relativePath = normalizedPath.substring(publicPrefix.length());
        } else if (normalizedPath.startsWith(privatePrefix)) {
            relativePath = normalizedPath.substring(privatePrefix.length());
        } else {
            // 如果不包含前缀，就直接返回原始路径
            relativePath = normalizedPath;
        }

        // 返回时保持 Windows 风格（\）
        return relativePath.replace('/', '\\');
    }


    @Override
    public void deleteTeachingMaterialsByIds(List<Long> ids) {
        // 1. 查出这些素材
        List<TeachingMaterials> materialsList = this.listByIds(ids);

        // 2. 遍历删除文件
        for (TeachingMaterials materials : materialsList) {
            Path filePath = Paths.get(materials.getFilePath());

            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，文件删除失败");
            }
        }
        // 3. 删除数据库记录
        this.removeByIds(ids);
    }

}
