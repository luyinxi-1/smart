package com.upc.modular.homepage.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.homepage.entity.HomePagePromotion;
import com.upc.modular.homepage.entity.HomePageTextbook;
import com.upc.modular.homepage.mapper.HomePagePromotionMapper;
import com.upc.modular.homepage.mapper.HomePageTextbookMapper;
import com.upc.modular.homepage.param.HomePagePromotionListSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionPageSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionReturnParam;
import com.upc.modular.homepage.param.HomePageTextbookReturnParam;
import com.upc.modular.homepage.service.IHomePagePromotionService;
import com.upc.modular.homepage.service.IHomePageTextbookService;
import com.upc.modular.textbook.param.TextbookPageReturnParam;
import com.upc.modular.textbook.service.ITextbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author JM
 * @since 2025-10-28
 */
@Service
public class HomePageTextbookServiceImpl extends ServiceImpl<HomePageTextbookMapper, HomePageTextbook> implements IHomePageTextbookService {
    @Autowired
    private HomePageTextbookMapper homePageTextbookMapper;
    @Autowired
    private ITextbookService textbookService;
    @Override
    public Boolean insert(HomePageTextbook homePageTextbook) {
        return this.save(homePageTextbook);
    }

    @Override
    public Boolean batchDelete(List<Long> idList) {
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        return this.removeBatchByIds(idList);
    }

    @Override
    public Boolean updateTextbook(HomePageTextbook homePageTextbook) {
        if (ObjectUtils.isEmpty(homePageTextbook)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.updateById(homePageTextbook);
    }

    @Override
    public List<HomePageTextbookReturnParam> getHomePageTextbook(HomePagePromotionListSearchParam param) {
        List<HomePageTextbookReturnParam> promotions = homePageTextbookMapper.selectTextbookListWithNames(param);
        promotions.forEach(this::setTextbookDetails);
        return promotions;
    }

    @Override
    public Page<HomePageTextbookReturnParam> getHomePageTextbookPage(HomePagePromotionPageSearchParam param) {
        Page<HomePageTextbookReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        Page<HomePageTextbookReturnParam> textbookPage = homePageTextbookMapper.selectTextbookPageWithNames(page, param);

        List<HomePageTextbookReturnParam> records = textbookPage.getRecords();

        records.forEach(this::setTextbookDetails);

        List<HomePageTextbookReturnParam> filteredRecords = records.stream()
                .filter(p -> p.getTextbook() != null)
                .collect(Collectors.toList());

        textbookPage.setRecords(filteredRecords);
        return textbookPage;
    }

    @Override
    public HomePageTextbookReturnParam getHomePageTextbookDetails(Long promotionId) {
        HomePageTextbookReturnParam textbookDetails = homePageTextbookMapper.getHomePageNoticeDetails(promotionId);
        setTextbookDetails(textbookDetails);
        return textbookDetails;
    }

    @Override
    public Boolean updateClearTextbook(List<HomePageTextbook> homePageTextbook) {
        this.remove(new MyLambdaQueryWrapper<>());

        return this.saveBatch(homePageTextbook);
    }

    private void setTextbookDetails(HomePageTextbookReturnParam home_textbook) {
        if (home_textbook == null || ObjectUtils.isEmpty(home_textbook.getTextbookId())) {
            return;
        }

        TextbookPageReturnParam textbook = textbookService.getOneTextbookDetails(home_textbook.getTextbookId());

        if (ObjectUtils.isNotEmpty(textbook)) {
            home_textbook.setTextbook(textbook);
        }
    }
}
