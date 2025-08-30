package com.upc.modular.textbook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.textbook.entity.TextbookAuthority;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.param.TextbookAuthorityReturnParam;
import com.upc.modular.textbook.param.TextbookAuthoritySearchParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-08
 */
public interface ITextbookAuthorityService extends IService<TextbookAuthority> {

    void deleteTextbookAuthorityByIds(List<Long> ids);

    void insertTextbookAuthority(TextbookAuthority textbookAuthority);

//    void updateTextbookAuthorityById(TextbookAuthority textbookAuthority);

    Page<TextbookAuthorityReturnParam> getTextbookAuthorityPage(TextbookAuthoritySearchParam param);

    boolean textbookAuthorityJudge(Long textBookId, Long userId);

    boolean textbookAuthorityEditJudge(Long textBookId, Long userId);
}
