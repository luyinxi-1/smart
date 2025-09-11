package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookReview;
import com.upc.modular.textbook.mapper.TextbookReviewMapper;
import com.upc.modular.textbook.param.TextbookReviewPageParam;
import com.upc.modular.textbook.service.ITextbookService;
import com.upc.modular.textbook.service.ITextbookReviewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fwx
 * @since 2025-08-12
 */
@Service
public class TextbookReviewServiceImpl extends ServiceImpl<TextbookReviewMapper, TextbookReview> implements ITextbookReviewService {

    @Autowired
    private ITextbookService textbookService;
    @Override
    public void insertTextbookReview(TextbookReview textbookReview) {

        if (ObjectUtils.isEmpty(textbookReview)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }

        Long textbookId = textbookReview.getTextbookId();
        Textbook textbook = textbookService.getById(textbookId);

        if (ObjectUtils.isEmpty(textbook)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "未找到ID为 " + textbookId + " 的教材");
        }

        // 设置教材版本号到审核记录中
        textbookReview.setTextbookVersionNumber(textbook.getVersionNumber());

        // 1. Update the textbook's review status to 2 (审核中)
        textbook.setReviewStatus(2);
        textbookService.updateById(textbook);

        // 2. Save the review record
        this.save(textbookReview);
    }

    @Override
    public Page<TextbookReview> getPageByTextbookId(TextbookReviewPageParam param) {
        Page<TextbookReview> page = new Page<>(param.getCurrent(), param.getSize());
        LambdaQueryWrapper<TextbookReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(param.getTextbookId() != null, TextbookReview::getTextbookId, param.getTextbookId());
        queryWrapper.orderByDesc(TextbookReview::getAddDatetime);
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional
    public void processReviewResult(Long reviewId, Integer auditResult, String description) {
        // 1. 获取审核记录
        TextbookReview review = this.getById(reviewId);
        if (review == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "未找到ID为 " + reviewId + " 的审核记录");
        }

        // 2. 更新审核记录结果
        review.setAuditResult(auditResult);
        review.setDescriptionOfAuditResults(description);
        this.updateById(review);

        // 3. 更新教材状态
        Textbook textbook = textbookService.getById(review.getTextbookId());
        if (textbook != null) {
            if (auditResult == 1) {
                // 审核通过
                textbook.setReviewStatus(1);
                textbook.setReleaseStatus(1); // 【新增】更新发布状态为"已发布"
                
                // 更新教材版本号
                String currentVersion = textbook.getVersionNumber();
                if (currentVersion != null && !currentVersion.isEmpty()) {
                    // 解析当前版本号并增加修订版本号
                    String[] parts = currentVersion.startsWith("v") ? currentVersion.substring(1).split("\\.") : currentVersion.split("\\.");
                    if (parts.length >= 2) {
                        try {
                            int major = Integer.parseInt(parts[0]);
                            int minor = Integer.parseInt(parts[1]);
                            // 增加修订版本号
                            String newVersion = "v" + major + "." + (minor + 1);
                            textbook.setVersionNumber(newVersion);
                        } catch (NumberFormatException e) {
                            // 如果解析失败，使用默认版本号格式
                            textbook.setVersionNumber("v1.0");
                        }
                    } else {
                        // 如果版本号格式不符合预期，使用默认版本号格式
                        textbook.setVersionNumber("v1.0");
                    }
                } else {
                    // 如果当前没有版本号，设置初始版本号
                    textbook.setVersionNumber("v1.0");
                }
            } else if (auditResult == 0) {
                // 审核未通过
                textbook.setReviewStatus(3);
                textbook.setReleaseStatus(0);
            }
            textbookService.updateById(textbook);
        }
    }
}