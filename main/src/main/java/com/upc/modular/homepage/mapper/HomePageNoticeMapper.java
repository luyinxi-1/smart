package com.upc.modular.homepage.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.homepage.entity.HomePageNotice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.homepage.param.HomePageNoticeListSearchParam;
import com.upc.modular.homepage.param.HomePageNoticePageSearchParam;
import com.upc.modular.homepage.param.HomePageNoticeReturnParam;
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
public interface HomePageNoticeMapper extends BaseMapper<HomePageNotice> {

    HomePageNoticeReturnParam getHomePageNoticeDetails(@Param("noticeId") Long noticeId);

    List<HomePageNoticeReturnParam> selectNoticeListWithNames(@Param("param") HomePageNoticeListSearchParam param);

    Page<HomePageNoticeReturnParam> selectNoticePageWithNames(Page<HomePageNoticeReturnParam> page, @Param("param") HomePageNoticePageSearchParam param);
    
    Page<HomePageNoticeReturnParam> selectNoticePageWithNamesAndCreator(Page<HomePageNoticeReturnParam> page, @Param("param") HomePageNoticePageSearchParam param, @Param("creatorId") Long creatorId);
}
