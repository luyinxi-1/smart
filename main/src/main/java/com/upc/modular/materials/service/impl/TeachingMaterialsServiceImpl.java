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
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.materials.controller.param.dto.TeachingMaterialsPageSearchDto;
import com.upc.modular.materials.controller.param.dto.TeachingMaterialsSaveOrUpdateParam;
import com.upc.modular.materials.controller.param.vo.MaterialsTextbookNameMappingReturnParam;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsInsertMaterialsReturnParam;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsReturnVo;
import com.upc.modular.materials.entity.MaterialsTextbookMapping;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.mapper.TeachingMaterialsMapper;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.service.impl.TeacherServiceImpl;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import com.upc.modular.textbook.service.ITextbookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ITextbookService textbookService;
    @Autowired
    private TextbookCatalogMapper textbookCatalogMapper;


    /**
     * 添加教学素材
     * <p>
     * // * @param files
     *
     * @param param
     * @return
     */
    @Override
    public TeachingMaterialsInsertMaterialsReturnParam insertMaterials(TeachingMaterialsSaveOrUpdateParam param) {
        TeachingMaterialsInsertMaterialsReturnParam result = new TeachingMaterialsInsertMaterialsReturnParam();

        // 1. 重名检查逻辑 (不变)
//        if (ObjectUtils.isNotEmpty(teachingMaterialsMapper.selectList(new LambdaQueryWrapper<TeachingMaterials>()
//                .eq(TeachingMaterials::getName, param.getName())
//                .eq(TeachingMaterials::getCreator, UserUtils.get().getId()))))
//            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，该命名素材已存在");

        TeachingMaterials teachingMaterials = new TeachingMaterials();
        BeanUtils.copyProperties(param, teachingMaterials); // 拷贝基础属性
        teachingMaterials.setId(null);
        teachingMaterials.setCreator(UserUtils.get().getId());
        teachingMaterials.setFileName(param.getFileName());
        if ("link".equals(param.getType())) {
            // 链接类型，路径由前端直接在JSON中提供
//            teachingMaterials.setFileName(UUID.randomUUID().toString()); // 生成一个虚拟文件名

        } else if ("imageSet".equals(param.getType())) {
            Path firstImagePath = Paths.get(param.getFileListPaths().get(0));
            String directoryPath = firstImagePath.getParent().toString();

            String imageSetLength = String.valueOf(param.getFileListPaths().size());
            String imageSetName = Paths.get(directoryPath).getFileName().toString(); // 从目录路径中获取 [uuid]_[length]

//            teachingMaterials.setFileName(imageSetName);
            teachingMaterials.setFilePath(directoryPath);

        }
        else if ("simulation".equalsIgnoreCase(param.getType()) || "H5".equalsIgnoreCase(param.getType())) {
            // 【修复点】：针对 H5/Simulation 获取真实带后缀的文件名

            // 1. 基础校验
            if (StringUtils.isBlank(param.getFilePath())) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "素材路径不能为空");
            }

            Path folderPath = Paths.get(param.getFilePath());
            // 假设 folderPath 是 .../20251125/8173b53e-20b1... (文件夹)

            String folderName = folderPath.getFileName().toString(); // 获取 UUID 文件夹名
            String finalFileName = folderName; // 默认值

            // 2. 获取父目录 (.../20251125/)
            Path parentPath = folderPath.getParent();

            // 3. 安全检查：必须判断 parentPath 是否为 null
            if (parentPath != null) {
                File parentDir = parentPath.toFile();

                // 4. 在父目录下查找同名压缩包
                if (parentDir.exists() && parentDir.isDirectory()) {
                    // 查找规则：名字以 "UUID." 开头，例如 "8173b53e....rar"
                    File[] matchingFiles = parentDir.listFiles((dir, name) ->
                            name.startsWith(folderName + ".") && !name.equals(folderName)
                    );

                    if (matchingFiles != null && matchingFiles.length > 0) {
                        // 找到了！直接使用第一个匹配的文件名（含后缀，如 .rar, .tgz, .zip）
                        finalFileName = matchingFiles[0].getName();
                    } else {
                        // 没找到（极少情况），手动补一个 .zip 后缀作为兜底
                        finalFileName = folderName + ".zip";
                    }
                }
            } else {
                // 如果没有父目录（例如路径就是根目录），兜底补后缀
                finalFileName = folderName + ".zip";
            }

//            teachingMaterials.setFileName(finalFileName);
            // 数据库存的文件路径依然是指向解压后的文件夹，因为 H5 需要访问里面的 index.html
            teachingMaterials.setFilePath(param.getFilePath());

        }
        else {
            // 其他文件类型
            // filePath 已经是完整的、最终的路径了
            Path filePathObj = Paths.get(param.getFilePath());
//            teachingMaterials.setFileName(filePathObj.getFileName().toString());
        }

        // 3. 保存到数据库
        if (this.save(teachingMaterials)) {
            // 如果提供了教材ID，则创建教材与素材的绑定关系
            if (param.getTextbookId() != null) {
                MaterialsTextbookMapping mapping = new MaterialsTextbookMapping();
                mapping.setTextbookId(param.getTextbookId());
                mapping.setMaterialId(teachingMaterials.getId());
                mapping.setChapterId(param.getChapterId());// 设置章节ID
                mapping.setChapterId2(param.getChapterId2());//设置备用ID
                mapping.setCreator(UserUtils.get().getId());
                mapping.setAddDatetime(LocalDateTime.now());
                materialsTextbookMappingService.save(mapping);
            }

            if (teachingMaterials.getType().equals("link")) {
                result.setFilePath(teachingMaterials.getFilePath());
            }
//            result.setFileName(teachingMaterials.getFileName());

            result.setMaterialId(teachingMaterials.getId());

            return result;
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
        } else {
            filePath = Paths.get(materials.getFilePath());

            // 对于 H5 和 simulation 类型，如果 filePath 指向的是目录，则尝试查找同级的原始压缩包
            if (("H5".equalsIgnoreCase(materials.getType()) || "simulation".equalsIgnoreCase(materials.getType()))
                    && Files.isDirectory(filePath)) {
                log.debug("Attempting to find original archive for {} type material with directory path: {}",
                        materials.getType(), filePath);

                // 查找同级目录下的原始压缩包
                Path parent = filePath.getParent();
                String baseName = filePath.getFileName().toString();
                // 允许的压缩包后缀
                String[] exts = {".zip", ".7z", ".tar", ".tgz"};

                Path candidate = null;
                // 首先尝试精确匹配 baseName + ext
                for (String ext : exts) {
                    Path p1 = parent.resolve(baseName + ext);
                    if (Files.exists(p1) && Files.isRegularFile(p1)) {
                        candidate = p1;
                        break;
                    }
                }

                // 如果上面没找到，再退一步：在 parent 目录下遍历，
                // 找「文件名以 baseName 开头且后缀在 exts 内」的文件
                if (candidate == null) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent)) {
                        for (Path p : stream) {
                            if (Files.isRegularFile(p)) {
                                String name = p.getFileName().toString();
                                if (name.startsWith(baseName)) {
                                    for (String ext : exts) {
                                        if (name.endsWith(ext)) {
                                            candidate = p;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (candidate != null) {
                                break;
                            }
                        }
                    } catch (IOException e) {
                        log.warn("Failed to search directory for original archive: {}", parent, e);
                    }
                }

                // 如果找到压缩包，就用 candidate 作为真正的下载文件
                if (candidate != null) {
                    filePath = candidate;
                    log.debug("Found original archive for {} type material: {}", materials.getType(), filePath);
                } else {
                    // 找不到则记录 warn 日志并抛出业务异常
                    log.warn("Original archive not found for {} type material with directory path: {}, baseName: {}",
                            materials.getType(), filePath, baseName);
                    throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST, "原始压缩包不存在或已被删除");
                }
            }
        }

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

        // 1. 构建对 TeachingMaterials 的基础查询条件
        MyLambdaQueryWrapper<TeachingMaterials> queryWrapper = new MyLambdaQueryWrapper<>();
        queryWrapper
                .eq(ObjectUtils.isNotEmpty(param.getAuthorId()), TeachingMaterials::getCreator, param.getAuthorId())
                .like(ObjectUtils.isNotEmpty(param.getName()), TeachingMaterials::getName, param.getName())
                .eq(ObjectUtils.isNotEmpty(param.getType()), TeachingMaterials::getType, param.getType());

        boolean unboundOnly = Boolean.TRUE.equals(param.getUnboundOnly());
        Long textbookId = param.getTextbookId();
        Long chapterId = param.getChapterId();      // 旧字段，后面只在非 unboundOnly 时用到（如果你还需要）
        Long chapterId2 = param.getChapterId2();    // 备用章节ID（控制范围）

        // 添加创建人筛选条件：非管理员只能查看自己创建的素材
        if (userType != 0) { // 非管理员用户
            queryWrapper.eq(TeachingMaterials::getCreator, currentUserId);
        } else if (param.getAuthorId() != null) { // 管理员指定了作者ID
            queryWrapper.eq(TeachingMaterials::getCreator, param.getAuthorId());
        }

        /*
         * =========================
         *   2. unboundOnly 相关逻辑
         * =========================
         */
        if (unboundOnly) {

            // 3️⃣ 规则：凡是在 mapping 表中存在 chapter_id 有值的素材，都认为已经绑定章节，
            // 在「只看未绑定」模式下一律排除。
            List<Long> boundIds = materialsTextbookMappingService.list(
                            new LambdaQueryWrapper<MaterialsTextbookMapping>()
                                    .select(MaterialsTextbookMapping::getMaterialId)
                                    .isNotNull(MaterialsTextbookMapping::getChapterId)
                    ).stream()
                    .map(MaterialsTextbookMapping::getMaterialId)
                    .distinct()
                    .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(boundIds)) {
                queryWrapper.notIn(TeachingMaterials::getId, boundIds);
            }

            // 如果没有教材ID，只根据“没有绑定章节”的条件筛一遍就行（上面的 notIn 已经完成）
            if (textbookId != null) {

                // chapterId2 = 0 也当作 “未传 / 临时章节”
                boolean hasRealChapter2 = (chapterId2 != null && chapterId2 > 0L);

                // 1️⃣ 和 2️⃣ 的核心都是先在 mapping 表中算出“允许出现的 materialId”
                LambdaQueryWrapper<MaterialsTextbookMapping> allowWrapper =
                        new LambdaQueryWrapper<MaterialsTextbookMapping>()
                                .select(MaterialsTextbookMapping::getMaterialId)
                                .eq(MaterialsTextbookMapping::getTextbookId, textbookId)
                                .isNull(MaterialsTextbookMapping::getChapterId);  // 只看未绑定正式章节的记录

                if (!hasRealChapter2) {
                    // 1️⃣ 规则：chapterId2 = 0 或者不传
                    // 只要 textbookId 对应，且 chapterId 和 chapterId2 都是空的
                    allowWrapper.isNull(MaterialsTextbookMapping::getChapterId2);
                } else {
                    // 2️⃣ 规则：chapterId2 有值（已入库）
                    // textbookId 对应，chapterId 为空，
                    // 且 (chapterId2 = 传入参数 或 chapterId2 仍然为空)
                    allowWrapper.and(w -> w
                            .eq(MaterialsTextbookMapping::getChapterId2, chapterId2)
                            .or()
                            .isNull(MaterialsTextbookMapping::getChapterId2)
                    );
                }

                List<Long> allowedIds = materialsTextbookMappingService.list(allowWrapper)
                        .stream()
                        .map(MaterialsTextbookMapping::getMaterialId)
                        .distinct()
                        .collect(Collectors.toList());

                if (!CollectionUtils.isEmpty(allowedIds)) {
                    // 其它筛选条件 AND id IN allowedIds AND id NOT IN boundIds
                    queryWrapper.in(TeachingMaterials::getId, allowedIds);
                } else {
                    // 没有任何符合条件的素材，直接让结果为空
                    queryWrapper.isNull(TeachingMaterials::getId);
                }
            }

        } else {
            /*
             * =========================
             *   3. unboundOnly = false
             *      正常查询绑定记录
             * =========================
             *
             * 要求：直接根据 chapterId2（以及 textbookId 等）进行过滤，
             * 不再强制要求 chapterId 为空。
             */
            if (textbookId != null || chapterId2 != null || chapterId != null) {

                LambdaQueryWrapper<MaterialsTextbookMapping> mappingWrapper =
                        new LambdaQueryWrapper<MaterialsTextbookMapping>()
                                .select(MaterialsTextbookMapping::getMaterialId)
                                .eq(textbookId != null, MaterialsTextbookMapping::getTextbookId, textbookId);

                // 优先用 chapterId2 过滤；如果你还想兼容旧的 chapterId，也可以再加一行 and/or
                if (chapterId2 != null && chapterId2 > 0L) {
                    mappingWrapper.eq(MaterialsTextbookMapping::getChapterId2, chapterId2);
                } else if (chapterId != null) {
                    // 如果前端传的是旧的 chapterId，这里兜底一下
                    mappingWrapper.eq(MaterialsTextbookMapping::getChapterId, chapterId);
                }

                List<Long> materialIds = materialsTextbookMappingService.list(mappingWrapper)
                        .stream()
                        .map(MaterialsTextbookMapping::getMaterialId)
                        .distinct()
                        .collect(Collectors.toList());

                if (!materialIds.isEmpty()) {
                    queryWrapper.in(TeachingMaterials::getId, materialIds);
                } else {
                    queryWrapper.isNull(TeachingMaterials::getId);
                }
            }
        }

        // 4. 排序
        queryWrapper.orderByDesc(TeachingMaterials::getAddDatetime);

        // 5. 执行一次数据库分页查询
        Page<TeachingMaterials> pageResult = this.page(
                new Page<>(param.getCurrent(), param.getSize()),
                queryWrapper
        );
        List<TeachingMaterials> materialsList = pageResult.getRecords();
        if (CollectionUtils.isEmpty(materialsList)) {
            return new Page<>(param.getCurrent(), param.getSize());
        }

        // 6. 后处理：查 mapping / 教材 / 章节 / 作者，组装 VO

        List<Long> materialIds = materialsList.stream()
                .map(TeachingMaterials::getId)
                .distinct()
                .collect(Collectors.toList());

        // 6.1 获取素材与教材的绑定关系
        List<MaterialsTextbookMapping> materialTextbookMappings = materialsTextbookMappingService.list(
                new LambdaQueryWrapper<MaterialsTextbookMapping>()
                        .in(MaterialsTextbookMapping::getMaterialId, materialIds)
        );
        Map<Long, MaterialsTextbookMapping> materialMappingMap = materialTextbookMappings.stream()
                .collect(Collectors.toMap(MaterialsTextbookMapping::getMaterialId, Function.identity(), (a, b) -> a));

        // 6.2 教材、章节名称
        Map<Long, String> textbookIdNameMap = new HashMap<>();
        Map<Long, Integer> textbookIdReleaseStatusMap = new HashMap<>(); // 添加教材状态映射
        Map<Long, String> chapterIdNameMap = new HashMap<>();
        if (!materialTextbookMappings.isEmpty()) {
            List<Long> textbookIds = materialTextbookMappings.stream()
                    .map(MaterialsTextbookMapping::getTextbookId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(textbookIds)) {
                List<Textbook> textbooks = textbookService.list(
                        new LambdaQueryWrapper<Textbook>().in(Textbook::getId, textbookIds)
                );
                textbookIdNameMap = textbooks.stream()
                        .collect(Collectors.toMap(Textbook::getId, Textbook::getTextbookName));
                textbookIdReleaseStatusMap = textbooks.stream()
                        .collect(Collectors.toMap(Textbook::getId, Textbook::getReleaseStatus)); // 获取教材状态
            }

            List<Long> chapterIds = materialTextbookMappings.stream()
                    .map(MaterialsTextbookMapping::getChapterId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(chapterIds)) {
                chapterIdNameMap = textbookCatalogMapper.selectList(
                        new LambdaQueryWrapper<TextbookCatalog>().in(TextbookCatalog::getId, chapterIds)
                ).stream().collect(Collectors.toMap(TextbookCatalog::getId, TextbookCatalog::getCatalogName));
            }
        }

        // 6.3 作者姓名
        List<Long> authorIdList = materialsList.stream()
                .map(TeachingMaterials::getCreator)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> userIdNameMap;
        if (ObjectUtils.isNotEmpty(authorIdList)) {
            userIdNameMap = sysUserService.list(
                    new LambdaQueryWrapper<SysTbuser>().in(SysTbuser::getId, authorIdList)
            ).stream().collect(Collectors.toMap(SysTbuser::getId, SysTbuser::getNickname));
        } else {
            userIdNameMap = new HashMap<>();
        }

        // 7. 转 VO
        final Map<Long, String> finaltextbookIdNameMap = textbookIdNameMap;
        final Map<Long, Integer> finaltextbookIdReleaseStatusMap = textbookIdReleaseStatusMap; // 教材状态映射
        final Map<Long, String> finalChapterIdNameMap = chapterIdNameMap;

        List<TeachingMaterialsReturnVo> pageRecordsVO = materialsList.stream()
                .map(materials -> {
                    TeachingMaterialsReturnVo temp = new TeachingMaterialsReturnVo();
                    BeanUtils.copyProperties(materials, temp);
                    temp.setAuthorName(userIdNameMap.get(materials.getCreator()));

                    // 是否为自己创建
                    if (materials.getCreator() != null) {
                        temp.setIsCreator(materials.getCreator().equals(currentUserId));
                    } else {
                        temp.setIsCreator(false);
                    }

                    // 设置教材ID/名称、章节ID/名称（这里仍然用 chapterId，如果你需要展示 chapterId2 可以再补字段）
                    MaterialsTextbookMapping mapping = materialMappingMap.get(materials.getId());
                    if (mapping != null) {
                        temp.setTextbookId(mapping.getTextbookId());
                        temp.setTextbookName(finaltextbookIdNameMap.get(mapping.getTextbookId()));
                        temp.setChapterId(mapping.getChapterId());
                        temp.setChapterId2(mapping.getChapterId2());
                        temp.setChapterName(finalChapterIdNameMap.get(mapping.getChapterId()));
                        // 设置教材状态
                        temp.setReleaseStatus(finaltextbookIdReleaseStatusMap.get(mapping.getTextbookId()));
                    }

                    return temp;
                })
                .collect(Collectors.toList());

        // 8. 组装分页返回
        Page<TeachingMaterialsReturnVo> resultPage =
                new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        resultPage.setRecords(pageRecordsVO);
        return resultPage;
    }

    @Override
    public TeachingMaterialsReturnVo getTeachingMaterials(Long id, Long textbookId) {
        if (id == null)
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数 id 不能为空");

        TeachingMaterials materials = this.getById(id);
        if (ObjectUtils.isEmpty(materials))
            throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST);

        Long currentUserId = UserUtils.get().getId();
        Integer userType = UserUtils.get().getUserType();

        // 权限检查逻辑
/*        if (!materials.getIsPublic() && !materials.getCreator().equals(currentUserId) && userType != 0) {
            if (textbookId == null)
                throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "，没有权限查看此文件");
            List<MaterialsTextbookMapping> textbookMappingList = materialsTextbookMappingService.list(
                    new LambdaQueryWrapper<MaterialsTextbookMapping>()
                            .eq(MaterialsTextbookMapping::getMaterialId, id)
                            .eq(MaterialsTextbookMapping::getTextbookId, textbookId));
            if (textbookMappingList.size() == 0)
                throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "，没有权限查看此文件");
        }*/

        TeachingMaterialsReturnVo materialsReturnVo = new TeachingMaterialsReturnVo();
        BeanUtils.copyProperties(materials, materialsReturnVo);

        // 设置文件名
        materialsReturnVo.setFileName(materials.getFileName());

        // 设置作者名
        Teacher teacher = teacherService.getById(materials.getCreator());
        if (teacher != null && ObjectUtils.isNotEmpty(teacher.getName())) {
            materialsReturnVo.setAuthorName(teacher.getName());
        }

        if (materials.getCreator() != null) {
            materialsReturnVo.setIsCreator(materials.getCreator().equals(currentUserId));
        } else {
            materialsReturnVo.setIsCreator(false);
        }

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
        if (ObjectUtils.isEmpty(oldData)) {
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

        String materialType = oldData.getType();

        // 5.1 如果是图集类型
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
                    // 优先使用前端传递的fileName，否则从路径中提取
                    if (ObjectUtils.isNotEmpty(param.getFileName())) {
                        oldData.setFileName(param.getFileName());
                    } else {
                        oldData.setFileName(parentDir.getFileName().toString());
                    }
                } catch (InvalidPathException e) {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "提供的图集文件路径格式无效");
                }
            }
        }
        // 5.2 如果是链接类型
        else if ("link".equals(materialType)) {
            // 更新链接的URL
            if (ObjectUtils.isNotEmpty(param.getFilePath())) {
                oldData.setFilePath(param.getFilePath());
            }
            // 优先使用前端传递的fileName，否则生成随机UUID
            if (ObjectUtils.isNotEmpty(param.getFileName())) {
                oldData.setFileName(param.getFileName());
            } else {
                oldData.setFileName(UUID.randomUUID().toString());
            }
        }
        // 5.3 如果是其他文件类型
        else {
            if (ObjectUtils.isNotEmpty(param.getFilePath())) {
                String newFilePath = param.getFilePath();
                oldData.setFilePath(newFilePath);
                try {
                    // 优先使用前端传递的fileName，否则从路径中提取
                    if (ObjectUtils.isNotEmpty(param.getFileName())) {
                        oldData.setFileName(param.getFileName());
                    } else {
                        Path path = Paths.get(newFilePath);
                        String newFileName = path.getFileName().toString();
                        oldData.setFileName(newFileName);
                    }
                } catch (InvalidPathException e) {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "提供的文件路径格式无效");
                }
            }
        }
        
        // 更新教材和章节ID关联信息
        if (param.getTextbookId() != null) {
            // 查询是否存在该素材与教材的映射关系
            MaterialsTextbookMapping mapping = materialsTextbookMappingService.getOne(
                new LambdaQueryWrapper<MaterialsTextbookMapping>()
                    .eq(MaterialsTextbookMapping::getMaterialId, oldData.getId())
            );
            
            if (mapping != null) {
                // 更新教材ID和章节ID
                mapping.setTextbookId(param.getTextbookId());
                if (param.getChapterId() != null) {
                    mapping.setChapterId(param.getChapterId());
                }
                if (param.getChapterId2() != null) {
                    mapping.setChapterId2(param.getChapterId2());
                }
                mapping.setOperator(UserUtils.get().getId());
                mapping.setOperationDatetime(LocalDateTime.now());
                materialsTextbookMappingService.updateById(mapping);
            } else {
                // 如果之前没有映射关系，则创建新的映射关系
                mapping = new MaterialsTextbookMapping();
                mapping.setMaterialId(oldData.getId());
                mapping.setTextbookId(param.getTextbookId());
                mapping.setChapterId(param.getChapterId());
                mapping.setChapterId2(param.getChapterId2());
                mapping.setCreator(UserUtils.get().getId());
                mapping.setAddDatetime(LocalDateTime.now());
                materialsTextbookMappingService.save(mapping);
            }
        } else if (param.getChapterId() != null) {
            // 只更新章节ID（教材ID未提供）
            MaterialsTextbookMapping mapping = materialsTextbookMappingService.getOne(
                new LambdaQueryWrapper<MaterialsTextbookMapping>()
                    .eq(MaterialsTextbookMapping::getMaterialId, oldData.getId())
            );
            
            if (mapping != null) {
                // 更新章节ID
                mapping.setChapterId(param.getChapterId());
                mapping.setChapterId2(param.getChapterId2());
                mapping.setOperator(UserUtils.get().getId());
                mapping.setOperationDatetime(LocalDateTime.now());
                materialsTextbookMappingService.updateById(mapping);
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
            try {
                if (materials.getType().equals("link")) {
                    // 链接类型没有实际文件，直接删除数据库记录
                    this.removeById(materials.getId());
                }
                else if (materials.getType().equals("imageSet")) {
                    // 图集类型删除整个目录
                    Path filePath = Paths.get(materials.getFilePath());
                    if (Files.exists(filePath)) {
                        // 只有文件存在时才尝试删除
                        FileUtils.deleteQuietly(filePath.toFile());
                    }
                    // 总是删除数据库记录
                    this.removeById(materials.getId());
                }
                // "simulation" 或 "H5" 类型：删除文件或文件夹 (处理方式相同)
                else if (materials.getType().equals("simulation") || materials.getType().equals("H5")) {
                    Path filePath = Paths.get(materials.getFilePath());
                    if (Files.exists(filePath)) {
                        // 检查路径是指向文件还是目录
                        if (Files.isDirectory(filePath)) {
                            // 如果是目录，递归删除整个目录及其内容
                            FileUtils.deleteDirectory(filePath.toFile());
                        } else {
                            // 如果是文件，直接删除
                            Files.deleteIfExists(filePath);
                        }
                    }
                    // 总是删除数据库记录
                    this.removeById(materials.getId());
                }
                else {
                    // 其他文件类型
                    Path filePath = Paths.get(materials.getFilePath());
                    if (Files.exists(filePath)) {
                        // 只有文件存在时才尝试删除
                        Files.deleteIfExists(filePath);
                    }
                    // 总是删除数据库记录
                    this.removeById(materials.getId());
                }
            } catch (IOException e) {
                // 记录异常但继续处理其他素材
                e.printStackTrace();
                // 即使文件删除失败也删除数据库记录，避免数据不一致
                this.removeById(materials.getId());
                // 可以考虑添加日志记录此处发生的错误
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

    @Override
    public List<TeachingMaterials> getTeachingMaterialsByIds(Long textbookId) {

        List<Long> ids = materialsTextbookMappingService.selectMaterialsTextbookMappingByTextbookId(textbookId).stream()
                .map(MaterialsTextbookMapping::getMaterialId).collect(Collectors.toList());

    /*    if (ids.isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }*/
        if (ids.isEmpty()) {
            // 表示这个教材目前没有绑定任何教学素材，业务上属于“正常但为空”的情况
            return Collections.emptyList();
        }

        List<TeachingMaterials> teachingMaterialsList = this.listByIds(ids);

        return teachingMaterialsList;
    }
}
