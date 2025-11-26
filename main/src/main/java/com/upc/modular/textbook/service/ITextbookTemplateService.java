package com.upc.modular.textbook.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.upc.modular.textbook.entity.TextbookTemplate;

public interface ITextbookTemplateService {

    // 新增教材时调用：如果该教材还没有任何模板，则为它创建一个默认模板，status = 1
    void initDefaultTemplate(Long textbookId);

    // 新增一个模板（必须带 textbookId），内部保证同一本教材最多只有一个 status=1
    void addTemplate(TextbookTemplate template);

    // 根据主键 id 删除模板（删除时只需要 id）
    void deleteById(Long id);

    // 分页查询：根据教材ID + status 分页查询模板列表
    IPage<TextbookTemplate> pageByTextbookIdAndStatus(Long textbookId, Long status, long pageNo, long pageSize);

    // 设置启用模板：指定某个模板为当前启用（status=1），该教材其它模板 status 变为 0
    void setActiveTemplate(Long textbookId, Long templateId);

    // （可选）查询某教材当前启用模板
    TextbookTemplate getActiveTemplate(Long textbookId);
}