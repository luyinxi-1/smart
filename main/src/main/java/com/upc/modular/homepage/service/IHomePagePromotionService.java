package com.upc.modular.homepage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.homepage.entity.HomePagePromotion;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.homepage.param.HomePagePromotionListSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionPageSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionReturnParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-15
 */
public interface IHomePagePromotionService extends IService<HomePagePromotion> {

    Boolean insert(HomePagePromotion homePagePromotion);

    Boolean batchDelete(List<Long> idList);

    Boolean updatePromotion(HomePagePromotion homePagePromotion);

    List<HomePagePromotionReturnParam> getHomePagePromotion(HomePagePromotionListSearchParam param);

    Page<HomePagePromotionReturnParam> getHomePagePromotionPage(HomePagePromotionPageSearchParam param);

    HomePagePromotionReturnParam getHomePagePromotionDetails(Long promotionId);
}
