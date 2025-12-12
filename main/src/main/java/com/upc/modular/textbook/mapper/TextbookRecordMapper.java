package com.upc.modular.textbook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.textbook.param.TextbookRecordPageDto;
import com.upc.modular.textbook.param.TextbookRecordPageParam;
import com.upc.modular.textbook.entity.TextbookRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TextbookRecordMapper extends BaseMapper<TextbookRecord> {

    IPage<TextbookRecordPageDto> selectRecordPage(
            Page<?> page,
            @Param("param") TextbookRecordPageParam param
    );

}
