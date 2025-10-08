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
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
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
    public void batchUpdateCatalog(IdeologicalMaterialBatchUpdateCatalogParam param) {
        if (param == null || CollectionUtils.isEmpty(param.getIds()) || param.getTextbookCatalogId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        
        // 批量更新章节ID
        LambdaQueryWrapper<IdeologicalMaterial> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(IdeologicalMaterial::getId, param.getIds());
        
        IdeologicalMaterial updateEntity = new IdeologicalMaterial();
        updateEntity.setTextbookCatalogId(param.getTextbookCatalogId());
        
        this.update(updateEntity, queryWrapper);
    }

    private String replaceBase64PicToUrl(String content, String addressPrefix) {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateString = currentDate.format(formatter);

        String path = "/upload/public/picture/" +  dateString + "/";
        String ip = "";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String input = "src=\\s*\"?(.*?)(\"|>|\\s+)";
        Pattern p = Pattern.compile(input);
        Matcher matcher = p.matcher(content);
        while (matcher.find()) {
            String ret = matcher.group(1);
            if (ret.contains("data:")) {
                String filename = System.currentTimeMillis() + (int) (1 + Math.random() * 1000) + "." + ret.substring(ret.indexOf("/") + 1, ret.indexOf(";"));
                GenerateImage(ret.substring(ret.indexOf(",")), path + filename);
                content = content.replace(ret, addressPrefix + "/upload/public/picture/"  + dateString + "/" + filename);
            }
        }
        return content;
    }
}
