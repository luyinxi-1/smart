package com.upc.modular.textbook.mapper;

import com.upc.modular.textbook.entity.TextbookCatalog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-07-08
 */
@Mapper
public interface TextbookCatalogMapper extends BaseMapper<TextbookCatalog> {

    /**
     * 根据章节名称关键词查询匹配的教材ID列表（每本教材只返回一个ID）
     * @param keywords 关键词列表
     * @return 教材ID列表
     */
    List<Long> selectDistinctTextbookIdsByCatalogName(@Param("keywords") List<String> keywords);

    /**
     * 根据章节内容关键词查询匹配的教材ID列表（每本教材只返回一个ID）
     * @param keywords 关键词列表
     * @return 教材ID列表
     */
    List<Long> selectDistinctTextbookIdsByContent(@Param("keywords") List<String> keywords);
}