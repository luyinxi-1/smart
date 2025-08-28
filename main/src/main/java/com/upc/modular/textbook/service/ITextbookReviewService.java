package com.upc.modular.textbook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.textbook.entity.TextbookReview;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.param.TextbookReviewPageParam;

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

    /**
     * 根据教材ID分页查询审核记录
     * @param param 查询参数
     * @return 分页结果
     */
    Page<TextbookReview> getPageByTextbookId(TextbookReviewPageParam param);
}
