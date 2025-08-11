package com.upc.modular.homepage.mapper;

import com.upc.modular.homepage.entity.HomePagePromotion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.homepage.param.HomePagePromotionReturnParam;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-07-15
 */
@Mapper
public interface HomePagePromotionMapper extends BaseMapper<HomePagePromotion> {

    HomePagePromotionReturnParam getHomePageNoticeDetails(Long promotionId);
}
