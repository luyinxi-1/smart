package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.textbook.entity.IdeologicalMaterial;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.IdeologicalMaterialMapper;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.param.IdeologicalMaterialBatchUpdateCatalogParam;
import com.upc.modular.textbook.param.IdeologicalMaterialInsertAndUpdateParam;
import com.upc.modular.textbook.param.IdeologicalMaterialSearchParam;
import com.upc.modular.textbook.service.IIdeologicalMaterialService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.service.ITextbookCatalogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.upc.utils.Base64Decode.GenerateImage;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-08
 */
@Service
@Slf4j
public class IdeologicalMaterialServiceImpl extends ServiceImpl<IdeologicalMaterialMapper, IdeologicalMaterial> implements IIdeologicalMaterialService {

    @Autowired
    private ITextbookCatalogService textbookCatalogService;

    @Autowired
    private TextbookMapper textbookMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public void deleteIdeologicalMaterialByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        this.removeBatchByIds(ids);
    }

    @Override
    public Long insertIdeologicalMaterial(IdeologicalMaterialInsertAndUpdateParam param) {
        if (param == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        // 确保不保存章节ID，即使前端传入了该参数
        param.setTextbookCatalogId(null);
        if (ObjectUtils.isNotEmpty(param.getContent())) {
            param.setContent(replaceBase64PicToUrl(param.getContent(), param.getAddressPrefix()));
        }
        this.save(param);
        return param.getId();
    }

    @Override
    public void updateIdeologicalMaterialById(IdeologicalMaterialInsertAndUpdateParam param) {
        if (param == null || param.getId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        // 确保不保存章节ID，即使前端传入了该参数
        param.setTextbookCatalogId(null);
        String processedContent = param.getContent();
        if (processedContent.contains("data:image")) {
            processedContent = replaceBase64PicToUrl(processedContent, param.getAddressPrefix());
        }
        param.setContent(processedContent);
        this.updateById(param);
    }

    @Override
    public List<IdeologicalMaterial> getIdeologicalMaterialByConditions(IdeologicalMaterialSearchParam param) {
        LambdaQueryWrapper<IdeologicalMaterial> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(param.getType()), IdeologicalMaterial::getType, param.getType());
        queryWrapper.like(StringUtils.isNotBlank(param.getName()), IdeologicalMaterial::getName, param.getName());
        queryWrapper.eq(param.getTextbookId() != null, IdeologicalMaterial::getTextbookId, param.getTextbookId());
        queryWrapper.eq(param.getTextbookCatalogId() != null, IdeologicalMaterial::getTextbookCatalogId, param.getTextbookCatalogId());

        List<IdeologicalMaterial> ideologicalMaterialList = this.list(queryWrapper);

        if (ideologicalMaterialList.isEmpty()) {
            return ideologicalMaterialList;
        }

        // 构造临时结构体：包装排序字段
        List<Pair<IdeologicalMaterial, Integer>> wrappedList = new ArrayList<>();

        for (IdeologicalMaterial material : ideologicalMaterialList) {
            Integer sort = 0;
            if (material.getTextbookCatalogId() != null && material.getTextbookCatalogId() != 0L) {
                TextbookCatalog catalog = textbookCatalogService.getById(material.getTextbookCatalogId());
                if (catalog != null && catalog.getSort() != null) {
                    sort = catalog.getSort();
                }
            }
            wrappedList.add(Pair.of(material, sort));
        }

        // 根据 sort 排序
        wrappedList.sort(Comparator.comparingInt(Pair::getRight));

        List<IdeologicalMaterial> resultIdeologicalMaterials = wrappedList.stream().map(Pair::getLeft).collect(Collectors.toList());
        for (IdeologicalMaterial resultIdeologicalMaterial : resultIdeologicalMaterials) {
            Textbook textbook = textbookMapper.selectById(resultIdeologicalMaterial.getTextbookId());
            if(textbook != null){
                SysTbuser sysTbuser = sysUserMapper.selectById(textbook.getCreator());
                if(sysTbuser != null){
                    resultIdeologicalMaterial.setNickname(sysTbuser.getNickname());
                }
                else {
                    resultIdeologicalMaterial.setNickname("");
                }
            }else {
                resultIdeologicalMaterial.setNickname("");
            }
        }

        return resultIdeologicalMaterials;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateCatalog(List<IdeologicalMaterialBatchUpdateCatalogParam> params) {
        // 1. 基本参数校验
        if (CollectionUtils.isEmpty(params)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "请求参数列表不能为空");
        }

        // 2. 遍历参数列表进行逐个更新
        for (IdeologicalMaterialBatchUpdateCatalogParam param : params) {
            if (param.getId() == null) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教学思政/活动ID不能为空");
            }

            // 校验章节ID和临时UUID至少要有一个
            if (param.getTextbookCatalogId() == null && StringUtils.isEmpty(param.getTextbookCatalogUuid())) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        "更新ID为 " + param.getId() + " 的记录时，章节ID和章节UUID必须至少提供一个");
            }

            Long finalCatalogId;

            // 优先使用章节ID
            if (param.getTextbookCatalogId() != null) {
                finalCatalogId = param.getTextbookCatalogId();
            } else {
                // 如果章节ID为空，则使用临时UUID去数据库查询对应的ID
                TextbookCatalog textbookCatalog = textbookCatalogService.getOne(new LambdaQueryWrapper<TextbookCatalog>()
                        .eq(TextbookCatalog::getCatalogUuid, param.getTextbookCatalogUuid()));

                // 如果根据UUID没有找到对应的章节，则抛出异常
                if (textbookCatalog == null) {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                            "根据提供的UUID: " + param.getTextbookCatalogUuid() + " 未找到对应的章节");
                }
                finalCatalogId = textbookCatalog.getId();
            }

            // 3. 执行单条更新
            IdeologicalMaterial updateEntity = new IdeologicalMaterial();
            updateEntity.setId(param.getId());
            updateEntity.setTextbookCatalogId(finalCatalogId);

            // 使用 updateById 更为精确，避免了构造复杂 a queryWrapper
            this.updateById(updateEntity);
        }
    }

//    private String replaceBase64PicToUrl(String content, String addressPrefix) {
//        // 获取当前日期
//        LocalDate currentDate = LocalDate.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//        String dateString = currentDate.format(formatter);
//
//        String path = "D:\\NewMakeFile" +  "/" + dateString + "/";
//        String input = "src=\\s*\"?(.*?)(\"|>|\\s+)";
//        Pattern p = Pattern.compile(input);
//        Matcher matcher = p.matcher(content);
//        while (matcher.find()) {
//            String ret = matcher.group(1);
//            if (ret.contains("data:")) {
//                String filename = System.currentTimeMillis() + (int) (1 + Math.random() * 1000) + "." + ret.substring(ret.indexOf("/") + 1, ret.indexOf(";"));
//                GenerateImage(ret.substring(ret.indexOf(",")), path + filename);
////                content = content.replace(ret, addressPrefix + "/upload/public/picture/"  + dateString + "/" + filename);
//                content = content.replace(ret, addressPrefix + "D:\\NewMakeFile/"  + dateString + "/" + filename);
//            }
//        }
//        return content;
//    }
public String replaceBase64PicToUrl(String content, String addressPrefix) {
    if (content == null || content.isEmpty()) {
        return content;
    }

    // 1. 准备通用的日期和路径
    LocalDate currentDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    String dateString = currentDate.format(formatter);
    String basePath = "/usr/local/src/upload/public/picture/" + dateString + "/";

    // 2. 判断content是纯Base64还是HTML
    if (content.trim().startsWith("data:image")) {
        // 模式一：整个content是Base64数据
        String newUrl = processAndSaveBase64Image(content.trim(), basePath, dateString, addressPrefix);
        // 如果处理成功，返回新URL；如果失败，返回原始内容
        return newUrl != null ? newUrl : content;
    } else {
        // 模式二：content是HTML，需要查找并替换所有Base64图片
        Pattern pattern = Pattern.compile("src\\s*=\\s*\"(data:image.*?)\"");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String base64Uri = matcher.group(1);
            String newUrl = processAndSaveBase64Image(base64Uri, basePath, dateString, addressPrefix);
            // 如果处理成功，用新URL替换；如果失败，保留原始Base64，避免图片丢失
            String replacement = (newUrl != null) ? "src=\"" + newUrl + "\"" : matcher.group(0);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

    /**
     * 处理单个Base64 URI，保存为图片文件，并返回可访问的URL。
     *
     * @param base64Uri     完整的Base64数据URI (例如 "data:image/png;base64,iVBOR...")
     * @param basePath      文件保存的基础路径 (例如 "D:\NewMakeFile/20251016/")
     * @param dateString    日期字符串 (例如 "20251016")
     * @param addressPrefix URL前缀
     * @return 成功则返回拼接好的URL，失败则返回null
     */
    private String processAndSaveBase64Image(String base64Uri, String basePath, String dateString, String addressPrefix) {
        try {
            // 提取文件扩展名和数据部分
            String fileExtension = base64Uri.substring(base64Uri.indexOf("/") + 1, base64Uri.indexOf(";"));
            String base64Data = base64Uri.substring(base64Uri.indexOf(",") + 1);

            // 生成文件名并调用保存方法
            String filename = System.currentTimeMillis() + "" + (int) (1 + Math.random() * 1000) + "." + fileExtension;
            boolean success = GenerateImage(base64Data, basePath + filename);

            if (success) {
                return addressPrefix + "/usr/local/src/upload/public/picture/" + dateString + "/" + filename;
            }
        } catch (Exception e) {
            // 如果在解析或处理base64Uri时出错，记录日志（可选）并返回失败
            // log.error("处理Base64字符串时出错", e);
        }
        return null; // 任何失败都返回null
    }
}