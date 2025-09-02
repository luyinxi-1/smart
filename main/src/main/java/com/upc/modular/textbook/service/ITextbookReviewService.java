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

    /**
     * 处理审核结果
     * @param reviewId 审核记录ID
     * @param auditResult 审核结果 1通过 0不通过
     * @param description 审核描述
     */
    void processReviewResult(Long reviewId, Integer auditResult, String description);

    /**
     * 修改审核记录
     * @param textbookReview 审核记录信息
     */
    void updateReview(TextbookReview textbookReview);
}
