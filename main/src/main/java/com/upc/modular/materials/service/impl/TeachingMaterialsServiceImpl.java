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
import com.upc.modular.materials.controller.param.dto.TeachingMaterialsSaveOrUpdateParam;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * 添加教学素材
     *
     * @param files
     * @param param
     * @return
     */
    @Override
    public Boolean insertMaterials(List<MultipartFile> files, TeachingMaterialsSaveOrUpdateParam param) {
        // 查看该作者是否有重名素材
        if (ObjectUtils.isNotEmpty(teachingMaterialsMapper.selectList(new LambdaQueryWrapper<TeachingMaterials>()
                .eq(TeachingMaterials::getName, param.getName())
                .eq(TeachingMaterials::getAuthorId, UserUtils.get().getId()))))
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，该命名素材已存在");
        TeachingMaterials teachingMaterials = new TeachingMaterials();

        if (param.getType().equals("link")) {
            // 处理链接素材：链接地址存在了filePath里，所以不需要处理
            if (ObjectUtils.isEmpty(param.getFilePath()))
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
            BeanUtils.copyProperties(param, teachingMaterials);
            teachingMaterials.setId(null);
            teachingMaterials.setAuthorId(UserUtils.get().getId());
            String urlName = UUID.randomUUID().toString();
            teachingMaterials.setAuthorId(UserUtils.get().getId());
            teachingMaterials.setFileName(urlName);
            // 链接地址存在了filePath里，所以不需要处理

        } else if (param.getType().equals("imageSet")) {
            // 处理图集素材
            // 公共素材路径：upload/teaching_materials/public/imageSet/yyyyMMdd/[uuid]_[length]/图片
            // 私有素材路径：upload/teaching_materials/private/用户id/imageSet/yyyyMMdd/[uuid]_[length]/图片
            Path folderPath;
            String imageSetLength = String.valueOf(files.size());
            String imageSetName = UUID.randomUUID() + "_" + imageSetLength;
            if (param.getIsPublic())
                folderPath = Paths.get("upload", "teaching_materials", "public",
                        "imageSet", FileManageUtil.yyyyMMddStr(), imageSetName);
            else
                folderPath = Paths.get("upload", "teaching_materials", "private",
                        UserUtils.get().getId().toString(),
                        "imageSet", FileManageUtil.yyyyMMddStr(), imageSetName);

            Long filesSize = 0L;
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String fileName = FileManageUtil.createFileName(file, String.valueOf(i + 1));
                String filePath = FileManageUtil.uploadFile(file, folderPath, fileName);
                if (ObjectUtils.isEmpty(filePath))
                    throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，上传失败");

                filesSize += file.getSize();
            }
            BeanUtils.copyProperties(param, teachingMaterials);
            teachingMaterials.setId(null);
            teachingMaterials.setAuthorId(UserUtils.get().getId());
            teachingMaterials.setFileName(imageSetName);
            teachingMaterials.setFileSize(Math.round(filesSize / (1024.0 * 1024.0) * 100) / 100.0);
            teachingMaterials.setFilePath(folderPath.toString());

        } else {
            if (!TeachingMaterials.SUPPORTED_TYPES.contains(param.getType()))
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，不支持该素材类型");
            // 处理文件类型素材
            // 公共素材路径：upload/teaching_materials/public/文件类型/yyyyMMdd/文件名
            // 私有素材路径：upload/teaching_materials/private/用户id/文件类型/yyyyMMdd/文件名
            Path folderPath;
            if (param.getIsPublic())
                folderPath = Paths.get("upload", "teaching_materials", "public",
                        param.getType(), FileManageUtil.yyyyMMddStr());
            else
                folderPath = Paths.get("upload", "teaching_materials", "private",
                        UserUtils.get().getId().toString(),
                        param.getType(), FileManageUtil.yyyyMMddStr());
            if (files.size() > 1)
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
            MultipartFile file = files.get(0);
            String fileName = FileManageUtil.createFileName(file);

            String filePath = FileManageUtil.uploadFile(file, folderPath, fileName);
            if (ObjectUtils.isEmpty(filePath))
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，上传失败");

            BeanUtils.copyProperties(param, teachingMaterials);
            teachingMaterials.setId(null);
            teachingMaterials.setAuthorId(UserUtils.get().getId());
            teachingMaterials.setFileName(fileName);
            teachingMaterials.setFileSize(Math.round(file.getSize() / (1024.0 * 1024.0) * 100) / 100.0);
            teachingMaterials.setFilePath(filePath);

        }
        return this.save(teachingMaterials);
    }

    /**
     * 获取文件素材
     *
     * @param id         素材id
     * @param imageSetId 图集的图id
     * @param textbookId 绑定的教材id
     * @param action     下载方式
     * @param response
     */
    @Override
    public void getFileMaterials(Long id, Integer imageSetId, Long textbookId, String action, HttpServletResponse response) {
        try {
            if (ObjectUtils.isEmpty(id))
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数不能为空");

            // 获取并验证教学材料
            TeachingMaterials materials = teachingMaterialsMapper.selectById(id);
            if (ObjectUtils.isEmpty(materials))
                throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST, "，文件不存在");

            // 验证权限，如果是从教材中下载，则增加下载查看次数用于数据统计
            if (!validateDownloadPermission(materials, textbookId, action))
                throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "，没有下载权限");

            // 执行文件下载
            performFileDownload(materials, imageSetId, action, response);

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
     * 获取链接素材
     *
     * @param id
     * @param textbookId
     * @return
     */
    @Override
    public String getLinkMaterials(Long id, Long textbookId) {

        if (ObjectUtils.isEmpty(id))
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数不能为空");

        // 获取并验证教学材料
        TeachingMaterials materials = teachingMaterialsMapper.selectOne(new LambdaQueryWrapper<TeachingMaterials>().eq(TeachingMaterials::getId, id));
        if (ObjectUtils.isEmpty(materials))
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，链接不存在");
        if (!materials.getType().equals("link"))
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数错误");

        // 验证权限，如果是从教材中下载，则增加下载查看次数用于数据统计
        if (!validateDownloadPermission(materials, textbookId, "view"))
            throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "，没有查看权限");
        // 链接存在filePath字段
        return materials.getFilePath();
    }

    /**
     * 验证下载权限，如果从教材中下载，则增加下载查看次数用于数据统计
     *
     * @param materials  教学材料对象
     * @param textbookId 验证的教材ID
     * @param action     在线查看/下载
     */
    private Boolean validateDownloadPermission(TeachingMaterials materials, Long textbookId, String action) {
        if (ObjectUtils.isNotEmpty(textbookId)) {
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
                if (action.equals("view"))
                    mapping.setViewCount(mapping.getViewCount() + 1);
                else if (action.equals("download"))
                    mapping.setDownloadCount(mapping.getDownloadCount() + 1);
                else throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数错误");
                materialsTextbookMappingService.updateById(mapping);
                return true;
            }
        }

        // 如果是公开素材或是管理员用户，则无需验证权限
        if (materials.getIsPublic() || UserUtils.get().getUserType() == 0)
            return true;

        // 如果是作者本人，则拥有权限
        Long currentUserId = UserUtils.get().getId();
        if (materials.getAuthorId().equals(currentUserId))
            return true;

        return false;
    }

    /**
     * 执行文件下载
     */
    private void performFileDownload(TeachingMaterials materials, Integer imageSetId, String action, HttpServletResponse response) {
        Path filePath;
        if (materials.getType().equals("imageSet")) {
            Path dir = Paths.get(materials.getFilePath());
            List<Path> files;
            try (Stream<Path> stream = Files.walk(dir)) {
                files = stream.filter(Files::isRegularFile).collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，获取图片失败");
            }
            if (imageSetId < 0 || imageSetId >= files.size())
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，图片不存在");
            filePath = files.get(imageSetId);
        } else
            filePath = Paths.get(materials.getFilePath());
        if (!Files.exists(filePath))
            throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST, "，文件不存在");

        // 构造下载文件名
        String downloadFileName;
        if (materials.getType().equals("imageSet"))
            downloadFileName = materials.getFileName() + "_" + filePath.getFileName().toString();
        else
            downloadFileName = filePath.getFileName().toString();

        // 设置响应头
        setupResponseHeaders(response, filePath, downloadFileName, action);

        // 执行文件传输
        FileManageUtil.transferFile(filePath, response);
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
    public void updateTeachingMaterialsById(TeachingMaterialsSaveOrUpdateParam teachingmaterials) {
        if (ObjectUtils.isEmpty(teachingmaterials) || ObjectUtils.isEmpty(teachingmaterials.getId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        TeachingMaterials oldData = this.getById(teachingmaterials.getId());
        // 验证权限：判断是否是作者或者管理员
        if (!oldData.getAuthorId().equals(UserUtils.get().getId()) && UserUtils.get().getUserType() != 0) {
            throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS);
        }
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
//        this.updateById(teachingmaterials.);
        this.updateById(null);
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

        // 定义前缀
        String publicPrefix = "upload/teaching_materials/public/";
        String privatePrefix = "upload/teaching_materials/private/";

        String relativePath;
        if (filePath.startsWith(publicPrefix)) {
            relativePath = filePath.substring(publicPrefix.length());
        } else if (filePath.startsWith(privatePrefix)) {
            relativePath = filePath.substring(privatePrefix.length());
        } else {
            // 如果不包含前缀，就直接返回原始路径
            relativePath = filePath;
        }

        return relativePath;
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
