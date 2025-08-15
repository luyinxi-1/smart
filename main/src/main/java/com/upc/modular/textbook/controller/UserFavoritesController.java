package com.upc.modular.textbook.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.common.utils.UserUtils;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.TextbookReview;
import com.upc.modular.textbook.entity.UserFavorites;
import com.upc.modular.textbook.param.TextbookPageReturnParam;
import com.upc.modular.textbook.param.TextbookPageSearchParam;
import com.upc.modular.textbook.param.UserFavoritesPageSearch;
import com.upc.modular.textbook.param.UserFavoritesVO;
import com.upc.modular.textbook.service.ITextbookReviewService;
import com.upc.modular.textbook.service.IUserFavoritesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author fwx
 * @since 2025-08-14
 */
@RestController
@RequestMapping("/user-favorites")
@Api(tags = "我的书架")
public class UserFavoritesController {

    @Autowired
    private IUserFavoritesService userFavoritesService;

    @ApiOperation(value = "新增收藏")
    @PostMapping("/insertUserFavorites")
    public R insertUserFavorites(@RequestParam("textbookId") Long textbookId) {
        userFavoritesService.insertUserFavorites(textbookId);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "删除收藏")
    @PostMapping("/deleteUserFavorites")
    public R deleteUserFavorites(@RequestBody IdParam idParam) {
        userFavoritesService.deleteUserFavorites(idParam);
        return R.commonReturn(200, "删除成功", "");
    }
    @ApiOperation(value = "分页查询收藏")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<UserFavoritesVO>> getPage(@RequestBody UserFavoritesPageSearch param) {
        Page<UserFavoritesVO> page = userFavoritesService.getPage(param);
        PageBaseReturnParam<UserFavoritesVO> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }





}
