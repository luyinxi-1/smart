package com.upc.modular.textbook.service;

import com.upc.modular.textbook.entity.TextbookReview;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fwx
 * @since 2025-08-12
 */
public interface ITextbookReviewService extends IService<TextbookReview> {

    void insertTextbookReview(TextbookReview textbookReview);
}
