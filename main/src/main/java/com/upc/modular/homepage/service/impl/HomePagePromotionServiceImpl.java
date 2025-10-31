package com.upc.modular.homepage.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.homepage.entity.HomePagePromotion;
import com.upc.modular.homepage.mapper.HomePagePromotionMapper;
import com.upc.modular.homepage.param.HomePagePromotionListSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionPageSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionReturnParam;
import com.upc.modular.homepage.service.IHomePagePromotionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.param.TextbookPageReturnParam;
import com.upc.modular.textbook.service.ITextbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-15
 */
@Service
public class HomePagePromotionServiceImpl extends ServiceImpl<HomePagePromotionMapper, HomePagePromotion> implements IHomePagePromotionService {

    @Autowired
    private HomePagePromotionMapper homePagePromotionMapper;
    @Autowired
    private ITextbookService textbookService;
    @Override
    public Boolean insert(HomePagePromotion homePagePromotion) {
        if (ObjectUtils.isEmpty(homePagePromotion)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.save(homePagePromotion);
    }

    @Override
    public Boolean batchDelete(List<Long> idList) {
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        return this.removeBatchByIds(idList);
    }

    @Override
    public Boolean updatePromotion(HomePagePromotion homePagePromotion) {
        if (ObjectUtils.isEmpty(homePagePromotion)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.updateById(homePagePromotion);
    }

    public List<HomePagePromotionReturnParam> getHomePagePromotion(HomePagePromotionListSearchParam param) {
        List<HomePagePromotionReturnParam> promotions = homePagePromotionMapper.selectPromotionListWithNames(param);
        promotions.forEach(this::setTextbookDetails);
        return promotions;
    }

    @Override
    public Page<HomePagePromotionReturnParam> getHomePagePromotionPage(HomePagePromotionPageSearchParam param) {
        Page<HomePagePromotionReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        Page<HomePagePromotionReturnParam> promotionPage = homePagePromotionMapper.selectPromotionPageWithNames(page, param);

        List<HomePagePromotionReturnParam> records = promotionPage.getRecords();

        records.forEach(this::setTextbookDetails);

        List<HomePagePromotionReturnParam> filteredRecords = records.stream()
                .filter(p -> p.getTextbook() != null)
                .collect(Collectors.toList());

        promotionPage.setRecords(filteredRecords);
        return promotionPage;
    }

    @Override
    public HomePagePromotionReturnParam getHomePagePromotionDetails(Long promotionId) {
        HomePagePromotionReturnParam promotionDetails = homePagePromotionMapper.getHomePageNoticeDetails(promotionId);
        setTextbookDetails(promotionDetails);
        return promotionDetails;
    }

    @Override
    public Boolean updateClearPromotion(List<HomePagePromotion> homePagePromotion) {
        this.remove(new MyLambdaQueryWrapper<>());

        return this.saveBatch(homePagePromotion);
    }

    private void setTextbookDetails(HomePagePromotionReturnParam promotion) {
        if (promotion == null || ObjectUtils.isEmpty(promotion.getTextbookId())) {
            return;
        }

        TextbookPageReturnParam textbook = textbookService.getOneTextbookDetails(promotion.getTextbookId());

        if (ObjectUtils.isNotEmpty(textbook)) {
            promotion.setTextbook(textbook);
        }
    }
}
