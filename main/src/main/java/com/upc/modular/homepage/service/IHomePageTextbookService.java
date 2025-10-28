package com.upc.modular.homepage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.homepage.entity.HomePagePromotion;
import com.upc.modular.homepage.entity.HomePageTextbook;
import com.upc.modular.homepage.param.HomePagePromotionListSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionPageSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionReturnParam;
import com.upc.modular.homepage.param.HomePageTextbookReturnParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author JM
 * @since 2025-10-28
 */
public interface IHomePageTextbookService extends IService<HomePageTextbook> {

    Boolean insert(HomePageTextbook homePageTextbook);

    Boolean batchDelete(List<Long> idList);

    Boolean updateTextbook(HomePageTextbook homePagePromotion);

    List<HomePageTextbookReturnParam> getHomePageTextbook(HomePagePromotionListSearchParam param);

    Page<HomePageTextbookReturnParam> getHomePageTextbookPage(HomePagePromotionPageSearchParam param);

    HomePageTextbookReturnParam getHomePageTextbookDetails(Long promotionId);

    Boolean updateClearTextbook(HomePageTextbook homePageTextbook);
}
