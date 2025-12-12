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

    public Page<TextbookRecordPageDto> pageRecords(TextbookRecordPageParam param) {
        Page<TextbookRecordPageDto> page = new Page<>(param.getPageNum(), param.getPageSize());

        IPage<TextbookRecordPageDto> dataPage =
                this.baseMapper.selectRecordPage(page, param);
        return (Page<TextbookRecordPageDto>) dataPage;
    }

}