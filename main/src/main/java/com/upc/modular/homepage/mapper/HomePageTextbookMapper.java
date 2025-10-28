package com.upc.modular.homepage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.homepage.entity.HomePageTextbook;
import com.upc.modular.homepage.param.HomePagePromotionListSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionPageSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionReturnParam;
import com.upc.modular.homepage.param.HomePageTextbookReturnParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author JM
 * @since 2025-10-28
 */
@Mapper
public interface HomePageTextbookMapper extends BaseMapper<HomePageTextbook> {

    List<HomePageTextbookReturnParam> selectTextbookListWithNames(HomePagePromotionListSearchParam param);

    Page<HomePageTextbookReturnParam> selectTextbookPageWithNames(Page<HomePageTextbookReturnParam> page, HomePagePromotionPageSearchParam param);

    HomePageTextbookReturnParam getHomePageNoticeDetails(Long promotionId);
}
