package com.upc.modular.homepage.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.homepage.entity.HomePageNotice;
import com.upc.modular.homepage.param.HomePageNoticeListSearchParam;
import com.upc.modular.homepage.param.HomePageNoticePageSearchParam;
import com.upc.modular.homepage.param.HomePageNoticeReturnParam;
import com.upc.modular.homepage.service.IHomePageNoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-07-15
 */
@RestController
@RequestMapping("/home-page-notice")
@Api(tags = "通知公告")
public class HomePageNoticeController {

    @Autowired
    private IHomePageNoticeService homePageNoticeService;

    @ApiOperation(value = "新增通知公告")
    @PostMapping("/insert")
    public R<Boolean> insert(@RequestBody HomePageNotice homePageNotice) {
        return R.ok(homePageNoticeService.insert(homePageNotice));
    }

    @ApiOperation(value = "删除通知公告")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestParam("idList") List<Long> idList) {
        return R.ok(homePageNoticeService.batchDelete(idList));
    }

    @ApiOperation(value = "更新通知公告")
    @PostMapping("/updateNotice")
    public R<Boolean> updateNotice(@RequestBody HomePageNotice homePageNotice) {
        return R.ok(homePageNoticeService.updateNotice(homePageNotice));
    }

    @ApiOperation(value = "首页通知公告展示")
    @PostMapping("/getHomePageNotice")
    public R<List<HomePageNotice>> getHomePageNotice(@RequestBody HomePageNoticeListSearchParam param) {
        return R.ok(homePageNoticeService.getHomePageNotice(param));
    }

    @ApiOperation(value = "首页通知公告查看更多")
    @PostMapping("/getHomePageNoticePage")
    public R<PageBaseReturnParam<HomePageNotice>> getHomePageNoticePage(@RequestBody HomePageNoticePageSearchParam param) {
        Page<HomePageNotice> page = homePageNoticeService.getHomePageNoticePage(param);
        PageBaseReturnParam<HomePageNotice> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "查看详情")
    @PostMapping("/getHomePageNoticeDetails")
    public R<HomePageNoticeReturnParam> getHomePageNoticeDetails(@RequestParam("noticeId") Long noticeId) {
        return R.ok(homePageNoticeService.getHomePageNoticeDetails(noticeId));
    }
}
