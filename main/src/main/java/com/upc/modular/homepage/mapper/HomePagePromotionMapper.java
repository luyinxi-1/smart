package com.upc.modular.homepage.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.homepage.entity.HomePagePromotion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.homepage.param.HomePagePromotionListSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionPageSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionReturnParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    List<HomePagePromotionReturnParam> selectPromotionListWithNames(@Param("param") HomePagePromotionListSearchParam param);

    Page<HomePagePromotionReturnParam> selectPromotionPageWithNames(Page<HomePagePromotionReturnParam> page, @Param("param") HomePagePromotionPageSearchParam param);
}
