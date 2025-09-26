package com.upc.modular.materials.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.utils.FileManageUtil;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.controller.param.dto.TeachingMaterialsPageSearchDto;
import com.upc.modular.materials.controller.param.dto.TeachingMaterialsSaveOrUpdateParam;
import com.upc.modular.materials.controller.param.vo.MaterialsTextbookNameMappingReturnParam;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsReturnVo;
import com.upc.modular.materials.entity.MaterialsTextbookMapping;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.mapper.TeachingMaterialsMapper;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.service.impl.TeacherServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
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
@Transactional
public class TeachingMaterialsServiceImpl extends ServiceImpl<TeachingMaterialsMapper, TeachingMaterials> implements ITeachingMaterialsService {

    @Autowired
    private TeachingMaterialsMapper teachingMaterialsMapper;

    @Autowired
    private MaterialsTextbookMappingServiceImpl materialsTextbookMappingService;

    @Autowired
    private TeacherServiceImpl teacherService;

    @Autowired
    private TeachingMaterialsMapper baseMapper;

    /**
     * 添加教学素材
     *
    // * @param files
     * @param param
     * @return
     */
    @Override
    public String insertMaterials(TeachingMaterialsSaveOrUpdateParam param) {
        // 1. 重名检查逻辑 (不变)
        if (ObjectUtils.isNotEmpty(teachingMaterialsMapper.selectList(new LambdaQueryWrapper<TeachingMaterials>()
                .eq(TeachingMaterials::getName, param.getName())
                .eq(TeachingMaterials::getCreator, UserUtils.get().getId()))))
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，该命名素材已存在");

        TeachingMaterials teachingMaterials = new TeachingMaterials();
        BeanUtils.copyProperties(param, teachingMaterials); // 拷贝基础属性
        teachingMaterials.setId(null);
        teachingMaterials.setCreator(UserUtils.get().getId());

        if ("link".equals(param.getType())) {
            // 链接类型，路径由前端直接在JSON中提供
            teachingMaterials.setFileName(UUID.randomUUID().toString()); // 生成一个虚拟文件名

        } else if ("imageSet".equals(param.getType())) {
            Path firstImagePath = Paths.get(param.getFileListPaths().get(0));
            String directoryPath = firstImagePath.getParent().toString();

            String imageSetLength = String.valueOf(param.getFileListPaths().size());
            String imageSetName = Paths.get(directoryPath).getFileName().toString(); // 从目录路径中获取 [uuid]_[length]

            teachingMaterials.setFileName(imageSetName);
            teachingMaterials.setFilePath(directoryPath);

        } else {
            // 其他文件类型
            // filePath 已经是完整的、最终的路径了
            Path filePathObj = Paths.get(param.getFilePath());
            teachingMaterials.setFileName(filePathObj.getFileName().toString());
        }

        // 3. 保存到数据库
        if (this.save(teachingMaterials)) {
            if (teachingMaterials.getType().equals("link")) {
                return teachingMaterials.getFilePath();
            }
            return teachingMaterials.getFileName();
        } else {
            return null;
        }
    }

    /**
     * 获取文件素材
     *
     * @param id         素材id
     * @param imageSetId 图集的图id
     * @param textbookId 绑定的教材id
     * @param action     在线查看/下载
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
        if (materials.getCreator().equals(currentUserId))
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

        // 从上下文中获取当前登录用户的信息
        Integer userType = UserUtils.get().getUserType();
        Long currentUserId = UserUtils.get().getId();
        // 角色权限判断：学生无权限
        if (userType == 1) {
            throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS);
        }
        // 对于管理员(0)和教师(2)，，都能查询全部素材
        MyLambdaQueryWrapper<TeachingMaterials> queryWrapper = new MyLambdaQueryWrapper<>();
        // 将所有查询参数作为可选的筛选条件
        queryWrapper
                // 当 param.getAuthorId() 不为空时，增加 `creator = ?` 条件
                .eq(ObjectUtils.isNotEmpty(param.getAuthorId()), TeachingMaterials::getCreator, param.getAuthorId())
                // 当 param.getName() 不为空时，增加 `name LIKE ?` 条件
                .like(ObjectUtils.isNotEmpty(param.getName()), TeachingMaterials::getName, param.getName())
                // 当 param.getType() 不为空时，增加 `type = ?` 条件
                .eq(ObjectUtils.isNotEmpty(param.getType()), TeachingMaterials::getType, param.getType());
        List<TeachingMaterials> materialsList = this.list(queryWrapper);
        List<Long> authorIdList = materialsList.stream().map(TeachingMaterials::getCreator).distinct().collect(Collectors.toList());
        Map<Long, String> teacherIdNameMap;
        if (ObjectUtils.isNotEmpty(authorIdList)) {
            teacherIdNameMap = teacherService.list(
                            new LambdaQueryWrapper<Teacher>().in(Teacher::getId, authorIdList))
                    .stream().collect(
                            Collectors.toMap(Teacher::getId, Teacher::getName));
        } else {
            teacherIdNameMap = new HashMap<>();
        }
        List<TeachingMaterialsReturnVo> pageRecordsVO = materialsList.stream()
                .map(materials -> {
                    TeachingMaterialsReturnVo temp = new TeachingMaterialsReturnVo();
                    BeanUtils.copyProperties(materials, temp);
                    temp.setAuthorName(teacherIdNameMap.get(materials.getCreator()));
                    // 判断是否为自己创建的逻辑
                    if (materials.getCreator() != null) {
                        temp.setIsCreator(materials.getCreator().equals(currentUserId));
                    } else {
                        temp.setIsCreator(false);
                    }
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
        if (!materials.getIsPublic() && !materials.getCreator().equals(UserUtils.get().getId()) && UserUtils.get().getUserType() != 0) {
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
        // 新增代码：设置文件名
        materialsReturnVo.setFileName(materials.getFileName());
        Teacher teacher = teacherService.getById(materials.getCreator());
        if (teacher != null && ObjectUtils.isNotEmpty(teacher.getName()))
            materialsReturnVo.setAuthorName(teacher.getName());

        return materialsReturnVo;
    }
@Override
@Transactional(rollbackFor = Exception.class)
public String updateTeachingMaterialsById(TeachingMaterialsSaveOrUpdateParam param) {
    // 1. 参数校验 (保持不变)
    if (ObjectUtils.isEmpty(param) || ObjectUtils.isEmpty(param.getId()))
        throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
    if (ObjectUtils.isEmpty(param.getType()))
        throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数 type 不能为空");
    TeachingMaterials oldData = this.getById(param.getId());
    if(ObjectUtils.isEmpty(oldData)){
        throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，未找到指定ID的素材");
    }
    if (!UserUtils.get().getId().equals(oldData.getCreator()) && UserUtils.get().getUserType() != 0) {
        throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS);
    }
    if (!param.getType().equals(oldData.getType()))
        throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，类型不可变");
    if (ObjectUtils.isNotEmpty(param.getName()))
        oldData.setName(param.getName());
    if (ObjectUtils.isNotEmpty(param.getCoverImagePath()))
        oldData.setCoverImagePath(param.getCoverImagePath());
    if (ObjectUtils.isNotEmpty(param.getQrcodePath()))
        oldData.setQrcodePath(param.getQrcodePath());
    if (ObjectUtils.isNotEmpty(param.getIsPublic())) {
        oldData.setIsPublic(param.getIsPublic());
    }

    // 5. 【核心修改】根据不同类型处理文件路径和文件名的更新
    String materialType = oldData.getType();

    // 5.1 如果是图集类型 (逻辑不变)
    if ("imageSet".equals(materialType)) {
        if (ObjectUtils.isNotEmpty(param.getFileListPaths())) {
            List<String> fileListPaths = param.getFileListPaths();
            String firstImagePath = fileListPaths.get(0);
            try {
                Path imagePath = Paths.get(firstImagePath);
                Path parentDir = imagePath.getParent();
                if (parentDir == null) {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "无法从图集路径中确定父目录");
                }
                oldData.setFilePath(parentDir.toString());
                oldData.setFileName(parentDir.getFileName().toString());
            } catch (InvalidPathException e) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "提供的图集文件路径格式无效");
            }
        }
    }
    // 5.2 如果是链接类型
    else if ("link".equals(materialType)) {
        // 更新链接的URL (存储在filePath字段)
        if (ObjectUtils.isNotEmpty(param.getFilePath())) {
            oldData.setFilePath(param.getFilePath());
        }
        oldData.setFileName(UUID.randomUUID().toString());
    }
    // 5.3 如果是其他文件类型 (逻辑不变)
    else {
        if (ObjectUtils.isNotEmpty(param.getFilePath())) {
            String newFilePath = param.getFilePath();
            oldData.setFilePath(newFilePath);
            try {
                Path path = Paths.get(newFilePath);
                String newFileName = path.getFileName().toString();
                oldData.setFileName(newFileName);
            } catch (InvalidPathException e) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "提供的文件路径格式无效");
            }
        }
    }
    // 6. 更新操作时间 (保持不变)
    oldData.setOperationDatetime(LocalDateTime.now());
    // 7. 执行数据库更新并返回结果 (保持不变)
    if (this.updateById(oldData)) {
        if ("link".equals(oldData.getType())) {
            return oldData.getFilePath();
        }
        return oldData.getFileName();
    } else {
        return null;
    }
}
    @Override
    public List<TeachingMaterials> getMaterialsByTextbookId(Long textbookId, String materialName) {
        List<MaterialsTextbookMapping> mappings = materialsTextbookMappingService.list(
                new LambdaQueryWrapper<MaterialsTextbookMapping>().eq(MaterialsTextbookMapping::getTextbookId, textbookId)
        );

        if (mappings == null || mappings.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> materialIds = mappings.stream()
                .map(MaterialsTextbookMapping::getMaterialId)
                .collect(Collectors.toList());
        if (materialIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<TeachingMaterials> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TeachingMaterials::getId, materialIds);
        if (StringUtils.isNotBlank(materialName)) {
            queryWrapper.like(TeachingMaterials::getName, materialName);
        }
        return teachingMaterialsMapper.selectList(queryWrapper);
    }
    @Override
    public void deleteTeachingMaterialsByIds(List<Long> ids) {
        // 1. 查出这些素材
        List<TeachingMaterials> materialsList = this.listByIds(ids);
        // 检查权限
        // 0-管理员
        if (UserUtils.get().getUserType() != 0) {
            materialsList.forEach(materials -> {
                if (!materials.getCreator().equals(UserUtils.get().getId()))
                    throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "，无权限删除");
            });
        }
        // 2. 遍历删除文件
        for (TeachingMaterials materials : materialsList) {
            if (materials.getType().equals("link"))
                this.removeById(materials.getId());
            else if (materials.getType().equals("imageSet")) {
                Path filePath = Paths.get(materials.getFilePath());
                if (FileUtils.deleteQuietly(filePath.toFile()))
                    this.removeById(materials.getId());
                else throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，部分素材删除失败");
            } else {
                try {
                    Path filePath = Paths.get(materials.getFilePath());
                    Files.deleteIfExists(filePath);
                    this.removeById(materials.getId());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，部分素材删除失败");
                }
            }
        }
    }

    @Override
    public MaterialsTextbookNameMappingReturnParam getMaterialsTextbookMappingByMaterialsId(List<Long> ids) {
        // 1. 查出这些素材
        List<TeachingMaterials> materialsList = this.listByIds(ids);
        // 检查权限
        // 0-管理员
        if (UserUtils.get().getUserType() != 0) {
            materialsList.forEach(materials -> {
                if (!materials.getCreator().equals(UserUtils.get().getId()))
                    throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "，无权限查看");
            });
        }
        // MaterialId-TextbookName
        return baseMapper.getMaterialIdToTextbookNameMap(ids);
    }
}
