package com.upc.modular.textbook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.Textbook;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.param.TextbookPageReturnParam;
import com.upc.modular.textbook.param.TextbookPageSearchParam;
import com.upc.modular.textbook.param.UserFavoritesPageSearch;

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

    List<Textbook> getNewTextbook(int getNumber);

    TextbookPageReturnParam getOneTextbookDetails(Long textbookId);

    Page<Textbook> getpageTextbookCenter(UserFavoritesPageSearch param);

    Textbook downloadTextbookInfo(Long textbookId);
}
