package com.upc.modular.textbook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.textbook.param.TextbookRecordPageDto;
import com.upc.modular.textbook.param.TextbookRecordPageParam;
import com.upc.modular.textbook.entity.TextbookRecord;
import com.baomidou.mybatisplus.extension.service.IService;
public interface ITextbookRecordService extends IService<TextbookRecord>{
    /**
     * 记录目录操作
     * @param textbookId 教材ID
     * @param catalogId  章节ID
     * @param status     1新增 2修改 3删除
     */
    void recordCatalogChange(Long textbookId, Long catalogId, Long status);
    Page<TextbookRecordPageDto> pageRecords(TextbookRecordPageParam param);

}
