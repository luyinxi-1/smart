package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookReview;
import com.upc.modular.textbook.mapper.TextbookReviewMapper;
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

        // 2. Update the textbook's review status to 2 (审核中)
        textbook.setReviewStatus(2);
        boolean updateResult = textbookService.updateById(textbook);

        this.save(textbookReview);
    }
}
