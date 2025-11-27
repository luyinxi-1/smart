package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.textbook.entity.TextbookTemplate;
import com.upc.modular.textbook.mapper.TextbookTemplateMapper;
import com.upc.modular.textbook.service.ITextbookTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TextbookTemplateServiceImpl extends ServiceImpl<TextbookTemplateMapper, TextbookTemplate> implements ITextbookTemplateService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initDefaultTemplate(Long textbookId) {
        // 检查参数
        if (textbookId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID不能为空");
        }

        // 查询该教材是否已经有任何模板记录
        Long count = this.lambdaQuery()
                .eq(TextbookTemplate::getTextbookId, textbookId)
                .count();

        // 如果已经有模板，不再创建默认模板
        if (count > 0) {
            return;
        }

        // 创建默认模板
        TextbookTemplate template = new TextbookTemplate();
        template.setTextbookId(textbookId);
        template.setTemplateName("默认模板");
        template.setLevel1Style("1");
        template.setLevel2Style("2");
        template.setLevel3Style("2");
        template.setLevel4Style("2");
        template.setThemeColor("1");
        template.setStatus(1L);
        
        this.save(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTemplate(TextbookTemplate template) {
        // 检查参数
        if (template.getTextbookId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID不能为空");
        }

        // 如果status为空，默认设为0L
        if (template.getStatus() == null) {
            template.setStatus(0L);
        }

        // 如果本次新增的模板 status != 1，直接保存
        if (!template.getStatus().equals(1L)) {
            this.save(template);
            return;
        }

        // 如果本次新增的模板 status == 1
        // 先将该 textbookId 下所有模板的 status 全部更新为 0L
        this.lambdaUpdate()
                .eq(TextbookTemplate::getTextbookId, template.getTextbookId())
                .set(TextbookTemplate::getStatus, 0L)
                .update();

        // 再插入当前这条模板，status = 1L
        template.setStatus(1L);
        this.save(template);
    }

    @Override
    public void deleteById(Long id) {
        // 检查是否存在
        if (id == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "模板ID不能为空");
        }
        
        TextbookTemplate template = this.getById(id);
        if (template == null) {
            throw new BusinessException(BusinessErrorEnum.NO_EXIT, "模板不存在");
        }
        
        this.removeById(id);
    }

    @Override
    public IPage<TextbookTemplate> pageByTextbookIdAndStatus(Long textbookId, Long status, long pageNo, long pageSize) {
        // 分页查询
        Page<TextbookTemplate> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<TextbookTemplate> wrapper = new LambdaQueryWrapper<>();
        
        // 如果提供了textbookId，则添加到查询条件中
        if (textbookId != null) {
            wrapper.eq(TextbookTemplate::getTextbookId, textbookId);
        }
        
        // 如果提供了status，则添加到查询条件中
        if (status != null) {
            wrapper.eq(TextbookTemplate::getStatus, status);
        }

        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setActiveTemplate(Long textbookId, Long templateId) {
        // 校验参数
        if (textbookId == null || templateId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID和模板ID不能为空");
        }

        // 根据templateId查询记录
        TextbookTemplate template = this.getById(templateId);
        if (template == null) {
            throw new BusinessException(BusinessErrorEnum.NO_EXIT, "模板不存在");
        }

        // 校验textbookId是否一致
        if (!template.getTextbookId().equals(textbookId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "模板不属于指定教材");
        }

        // 将该textbookId下所有模板的status统一更新为0L
        this.lambdaUpdate()
                .eq(TextbookTemplate::getTextbookId, textbookId)
                .set(TextbookTemplate::getStatus, 0L)
                .update();

        // 将templateId对应的记录status更新为1L
        this.lambdaUpdate()
                .eq(TextbookTemplate::getId, templateId)
                .set(TextbookTemplate::getStatus, 1L)
                .update();
    }

    @Override
    public TextbookTemplate getActiveTemplate(Long textbookId) {
        if (textbookId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID不能为空");
        }

        return this.lambdaQuery()
                .eq(TextbookTemplate::getTextbookId, textbookId)
                .eq(TextbookTemplate::getStatus, 1L)
                .one();
    }
}