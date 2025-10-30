package com.upc.modular.homepage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.homepage.entity.HomePagePromotion;
import com.upc.modular.homepage.entity.HomePageTextbook;
import com.upc.modular.homepage.param.HomePagePromotionListSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionPageSearchParam;
import com.upc.modular.homepage.param.HomePagePromotionReturnParam;
import com.upc.modular.homepage.param.HomePageTextbookReturnParam;
import com.upc.modular.homepage.service.IHomePagePromotionService;
import com.upc.modular.homepage.service.IHomePageTextbookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/home-page-textbook")
@Api(tags = "精选教材")
public class SelectedTextbooksController {
    @Autowired
    private IHomePageTextbookService homePageTextbooknService;

    @ApiOperation(value = "新增教材宣传")
    @PostMapping("/insert")
    public R<Boolean> insert(@RequestBody HomePageTextbook homePageTextbook) {
        return R.ok(homePageTextbooknService.insert(homePageTextbook));
    }

    @ApiOperation(value = "删除教材宣传")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestParam("idList") List<Long> idList) {
        return R.ok(homePageTextbooknService.batchDelete(idList));
    }

    @ApiOperation(value = "更新教材宣传")
    @PostMapping("/updateTextbook")
    public R<Boolean> updateTextbook(@RequestBody HomePageTextbook homePageTextbook) {
        return R.ok(homePageTextbooknService.updateTextbook(homePageTextbook));
    }

    @ApiOperation(value = "精选教材展示")
    @PostMapping("/getHomePageTextbook")
    public R<List<HomePageTextbookReturnParam>> getHomePageTextbook(@RequestBody HomePagePromotionListSearchParam param) {
        return R.ok(homePageTextbooknService.getHomePageTextbook(param));
    }

    @ApiOperation(value = "首页精选教材查看更多")
    @PostMapping("/getHomePageTextbookPage")
    public R<PageBaseReturnParam<HomePageTextbookReturnParam>> getHomePageTextbookPage(@RequestBody HomePagePromotionPageSearchParam param) {
        Page<HomePageTextbookReturnParam> page = homePageTextbooknService.getHomePageTextbookPage(param);
        PageBaseReturnParam<HomePageTextbookReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "查看详情")
    @PostMapping("/getHomePageTextbookDetails")
    public R<HomePageTextbookReturnParam> getHomePagePromotionDetails(@RequestParam("promotionId") Long promotionId) {
        return R.ok(homePageTextbooknService.getHomePageTextbookDetails(promotionId));
    }

    @ApiOperation(value = "清空式的更新操作")
    @PostMapping("/updateClearTextbook")
    public R<Boolean> updateClearPromotion(@RequestBody HomePageTextbook homePageTextbook) {
        return R.ok(homePageTextbooknService.updateClearTextbook(homePageTextbook));
    }
}
