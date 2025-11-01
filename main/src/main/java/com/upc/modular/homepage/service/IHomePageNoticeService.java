package com.upc.modular.homepage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.homepage.entity.HomePageNotice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.homepage.param.HomePageNoticeClassListParam;
import com.upc.modular.homepage.param.HomePageNoticeListSearchParam;
import com.upc.modular.homepage.param.HomePageNoticePageSearchParam;
import com.upc.modular.homepage.param.HomePageNoticeReturnParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-15
 */
public interface IHomePageNoticeService extends IService<HomePageNotice> {

    Boolean insert(HomePageNotice homePageNotice);

    Boolean batchDelete(List<Long> idList);

    Boolean updateNotice(HomePageNotice homePageNotice);

    List<HomePageNoticeReturnParam> getHomePageNotice(HomePageNoticeListSearchParam param);

    Page<HomePageNoticeReturnParam> getHomePageNoticePage(HomePageNoticePageSearchParam param);

    HomePageNoticeReturnParam getHomePageNoticeDetails(Long noticeId);

    Boolean insertTextbookNotice(List<HomePageNoticeClassListParam> homePageNotice);

    Page<HomePageNoticeReturnParam> getHomePageNoticePage2(HomePageNoticePageSearchParam param);
}
