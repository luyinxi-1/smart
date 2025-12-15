package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.utils.UserUtils;
import com.upc.modular.textbook.param.TextbookRecordPageDto;
import com.upc.modular.textbook.param.TextbookRecordPageParam;
import com.upc.modular.textbook.entity.TextbookRecord;
import com.upc.modular.textbook.mapper.TextbookRecordMapper;
import com.upc.modular.textbook.service.ITextbookRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TextbookRecordServiceImpl
        extends ServiceImpl<TextbookRecordMapper, TextbookRecord>
        implements ITextbookRecordService {
    @Autowired
    private com.upc.modular.auth.service.ISysUserService sysUserService;
    
    @Override
    public void recordCatalogChange(Long textbookId, Long catalogId,Long status) {

        // 当前用户
        Long userId = UserUtils.get().getId();

        TextbookRecord record = new TextbookRecord();
        record.setTextbookId(textbookId);
        record.setCatalogId(catalogId);
        record.setCreator(userId);
        record.setAddDatetime(LocalDateTime.now());
        record.setStatus(status);
        this.save(record);
    }

/*    public Page<TextbookRecordPageDto> pageRecords(TextbookRecordPageParam param) {
        Page<TextbookRecordPageDto> page = new Page<>(param.getPageNum(), param.getPageSize());

        IPage<TextbookRecordPageDto> dataPage =
                this.baseMapper.selectRecordPage(page, param);
        return (Page<TextbookRecordPageDto>) dataPage;
    }*/
public Page<TextbookRecordPageDto> pageRecords(TextbookRecordPageParam param) {
    // 1. 初始化分页对象
    Page<TextbookRecordPageDto> page = new Page<>(param.getPageNum(), param.getPageSize());

    // 2. 执行数据库查询
    IPage<TextbookRecordPageDto> dataPage = this.baseMapper.selectRecordPage(page, param);

    // 3. 【新增逻辑】遍历结果集，清洗 catalogName 字段
    List<TextbookRecordPageDto> records = dataPage.getRecords();
    if (records != null && !records.isEmpty()) {
        for (TextbookRecordPageDto record : records) {
            String originalName = record.getCatalogName();
            if (originalName != null && !originalName.isEmpty()) {
                // 使用正则去除 HTML 标签，并去除首尾空格
                String cleanName = originalName.replaceAll("<[^>]+>", "").trim();
                record.setCatalogName(cleanName);
            }
        }
    }

    return (Page<TextbookRecordPageDto>) dataPage;
}

}