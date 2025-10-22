package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.LearningNotes;
import com.upc.modular.textbook.mapper.LearningNotesMapper;
import com.upc.modular.textbook.param.LearningNotesPageReturnParam;
import com.upc.modular.textbook.param.LearningNotesPageSearchParam;
import com.upc.modular.textbook.param.UuidParam;
import com.upc.modular.textbook.service.ILearningNotesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-14
 */
@Service
public class LearningNotesServiceImpl extends ServiceImpl<LearningNotesMapper, LearningNotes> implements ILearningNotesService {
    @Autowired
    private LearningNotesMapper learningNotesMapper;
    
    @Override
    public Boolean insert(LearningNotes learningNotes) {
        if (ObjectUtils.isEmpty(learningNotes)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        if (ObjectUtils.isEmpty(UserUtils.get().getId())) {
            throw new BusinessException(BusinessErrorEnum.PLEASE_LOGIN, "用户未登录");
        }
        return this.save(learningNotes);
    }

    @Override
    public Boolean batchDelete(IdParam idParam) {
        if (ObjectUtils.isEmpty(idParam) || ObjectUtils.isEmpty(idParam.getIdList())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        return this.removeBatchByIds(idParam.getIdList());
    }

    @Override
    public Boolean batchDeleteByUuid(UuidParam uuidParam) {
        // 1. 参数校验
        if (ObjectUtils.isEmpty(uuidParam) || ObjectUtils.isEmpty(uuidParam.getUuidList())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参UUID列表不能为空");
        }

        // 2. 构建查询条件 (QueryWrapper)
        // 使用 LambdaQueryWrapper 可以防止硬编码字段名，更安全
        LambdaQueryWrapper<LearningNotes> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(LearningNotes::getClientUuid, uuidParam.getUuidList());

        // 3. 执行删除操作
        // this.remove(wrapper) 方法会根据构造的条件执行 DELETE FROM table WHERE ...
        return this.remove(wrapper);
    }

    @Override
    public Boolean updateNotes(LearningNotes param) {
        if (ObjectUtils.isEmpty(param)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        return this.updateById(param);
    }
    @Override
    public Boolean updateNotesbyClientUuid(LearningNotes param) {
        // 1. 基础参数校验
        if (ObjectUtils.isEmpty(param)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        // 2. 关键业务参数校验：必须提供 clientUuid 来确定要更新哪条记录
        if (StringUtils.isBlank(param.getClientUuid())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "clientUuid 不能为空");
        }

        // 3. 构建更新条件 (UpdateWrapper)
        // 使用 LambdaUpdateWrapper 保证类型安全，避免手写字段名出错
        LambdaUpdateWrapper<LearningNotes> wrapper = new LambdaUpdateWrapper<>();
        // 设置 WHERE 条件： WHERE client_uuid = #{param.clientUuid}
        wrapper.eq(LearningNotes::getClientUuid, param.getClientUuid());

        return this.update(param, wrapper);
    }

    @Override
    public Page<LearningNotesPageReturnParam> getAllPage(LearningNotesPageSearchParam param) {
        Page<LearningNotesPageReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        Page<LearningNotesPageReturnParam> resultPage = learningNotesMapper.getPage(page, param);

        for (LearningNotesPageReturnParam record : resultPage.getRecords()) {
            // 清洗章节名称
            if (record.getCatalogName() != null) {
                String rawHtml = record.getCatalogName();
                String plainText = Jsoup.parse(rawHtml).text(); // 去除HTML标签
//                plainText = extractMeaningfulCatalogName(plainText); // 可选：进一步提取章节核心内容
                record.setCatalogName(plainText);
            }
        }

        return resultPage;
    }

    @Override
    public LearningNotes getOneNote(Long id) {
        if (ObjectUtils.isEmpty(id)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        return learningNotesMapper.selectById(id);
    }

    @Override
    public Page<LearningNotesPageReturnParam> getMyPage(LearningNotesPageSearchParam param) {
        Page<LearningNotesPageReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        Long id = UserUtils.get().getId();
        Page<LearningNotesPageReturnParam> resultPage = learningNotesMapper.getMyPage(page, param, id);

        for (LearningNotesPageReturnParam record : resultPage.getRecords()) {
            // 清洗章节名称
            if (record.getCatalogName() != null) {
                String rawHtml = record.getCatalogName();
                String plainText = Jsoup.parse(rawHtml).text(); // 去除HTML标签
//                plainText = extractMeaningfulCatalogName(plainText); // 可选：进一步提取章节核心内容
                record.setCatalogName(plainText);
            }
        }

        return resultPage;
    }

    private String extractMeaningfulCatalogName(String text) {
        if (text == null) return null;
        // 用正则剔除前缀编号（如 1.1 或 2.3.4）
        return text.replaceFirst("^[\\d\\.]+\\s*", "").trim();
    }

}