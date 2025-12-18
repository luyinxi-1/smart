package com.upc.modular.textbook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.Textbook;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.param.*;

import java.util.List;
/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-08
 */
public interface ITextbookService extends IService<Textbook> {

    void insert(Textbook textbook);

    void deleteDictItemByIds(IdParam idParam);

    void updateTextbook(Textbook textbook);

    Page<TextbookPageReturnParam> getPage(TextbookPageSearchParam param);

    Page<Textbook> queryTextbooksByConditions(TextbookQueryReq req);

    List<Textbook> getNewTextbook(int getNumber);

    TextbookPageReturnParam getOneTextbookDetails(Long textbookId);

    Page<Textbook> getpageTextbookCenter(UserFavoritesPageSearch param);

    Textbook downloadTextbookInfo(Long textbookId);

    VersionCheckResultDto checkStatusAndVersion(Long textbookId, String clientVersion);

    Page<TextbookHotnessDto> getTextbookHotnessPage(Page<TextbookHotnessDto> page);

    Page<TextbookCenterPageReturnParam> getTextbookCenter(TextbookCenterPageSearchParam param);
    

    /**
     * 教材智能搜索（分页版本）
     *
     * @param query 搜索关键词，支持多个关键词（逗号或顿号分隔）
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页搜索结果
     */
    Page<TextbookIntelligentQueryReturnParam> smartSearch(String query, long current, long size);
    
    List<TextbookContentSearchResult> smartSearchInTextbook(Long textbookId, String query);
    
    /**
     * 批量更新教材的is_delete状态
     * @param ids 教材ID列表
     * @param isDelete 删除状态 1:已删除 0:未删除
     */
    void updateTextbookDeleteStatus(List<Long> ids, Integer isDelete);
}