package com.upc.modular.homepage.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.homepage.entity.HomePageNotice;
import com.upc.modular.homepage.entity.HomePagePromotion;
import com.upc.modular.homepage.mapper.HomePagePromotionMapper;
import com.upc.modular.homepage.param.HomePagePromotionListSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionPageSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionReturnParam;
import com.upc.modular.homepage.service.IHomePagePromotionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<HomePagePromotion> getHomePagePromotion(HomePagePromotionListSearchParam param) {
        return null;
    }

    @Override
    public Page<HomePagePromotion> getHomePagePromotionPage(HomePagePromotionPageSearchParam param) {
        return null;
    }

    @Override
    public HomePagePromotionReturnParam getHomePagePromotionDetails(Long promotionId) {
        return null;
    }
}
