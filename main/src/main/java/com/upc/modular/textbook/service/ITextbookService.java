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
    
    List<TextbookIntelligentQueryReturnParam> smartSearch(String query);
}
