package com.upc.modular.homepage.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.homepage.entity.HomePageNotice;
import com.upc.modular.homepage.entity.HomePagePromotion;
import com.upc.modular.homepage.param.*;
import com.upc.modular.homepage.service.IHomePageNoticeService;
import com.upc.modular.homepage.service.IHomePagePromotionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@RequestMapping("/home-page-promotion")
@Api(tags = "首页宣传")
public class HomePagePromotionController {
    @Autowired
    private IHomePagePromotionService homePagePromotionService;

    @ApiOperation(value = "新增首页宣传")
    @PostMapping("/insert")
    public R<Boolean> insert(@RequestBody HomePagePromotion homePagePromotion) {
        return R.ok(homePagePromotionService.insert(homePagePromotion));
    }

    @ApiOperation(value = "删除首页宣传")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestParam("idList") List<Long> idList) {
        return R.ok(homePagePromotionService.batchDelete(idList));
    }

    @ApiOperation(value = "更新首页宣传")
    @PostMapping("/updatePromotion")
    public R<Boolean> updatePromotion(@RequestBody HomePagePromotion homePagePromotion) {
        return R.ok(homePagePromotionService.updatePromotion(homePagePromotion));
    }

    @ApiOperation(value = "首页通知公告展示")
    @PostMapping("/getHomePagePromotion")
    public R<List<HomePagePromotionReturnParam>> getHomePagePromotion(@RequestBody HomePagePromotionListSearchParam param) {
        return R.ok(homePagePromotionService.getHomePagePromotion(param));
    }

    @ApiOperation(value = "首页通知公告查看更多")
    @PostMapping("/getHomePagePromotionPage")
    public R<PageBaseReturnParam<HomePagePromotionReturnParam>> getHomePagePromotionPage(@RequestBody HomePagePromotionPageSearchParam param) {
        Page<HomePagePromotionReturnParam> page = homePagePromotionService.getHomePagePromotionPage(param);
        PageBaseReturnParam<HomePagePromotionReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "查看详情")
    @PostMapping("/getHomePagePromotionDetails")
    public R<HomePagePromotionReturnParam> getHomePagePromotionDetails(@RequestParam("promotionId") Long promotionId) {
        return R.ok(homePagePromotionService.getHomePagePromotionDetails(promotionId));
    }

    @ApiOperation(value = "清空式的更新操作")
    @PostMapping("/updateClearPromotion")
    public R<Boolean> updateClearPromotion(@RequestBody List<HomePagePromotion> homePagePromotion) {
        return R.ok(homePagePromotionService.updateClearPromotion(homePagePromotion));
    }
}
