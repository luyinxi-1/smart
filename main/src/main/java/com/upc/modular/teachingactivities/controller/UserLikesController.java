package com.upc.modular.teachingactivities.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.teachingactivities.entity.UserLikes;
import com.upc.modular.teachingactivities.param.LikeStateParam;
import com.upc.modular.teachingactivities.service.IUserLikesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-07-07
 */
@RestController
@RequestMapping("/user-likes")
@Api(tags = "用户点赞")
public class UserLikesController {

    @Autowired
    private IUserLikesService userLikesService;

    @ApiOperation(value = "新增一条点赞信息")
    @PostMapping("/insertUserLike")
    public R insertUserLike(@RequestBody UserLikes UserLikes) {
        userLikesService.insertUserLike(UserLikes);
        return R.commonReturn(200, "新增成功", "");
    }

    /**
     * 根据id删除点赞信息
     * @param id
     * @return
     */
    @ApiOperation(value = "取消点赞")
    @PostMapping("/deleteUserLikeById")
    public R deleteUserLikeById(@RequestParam Long id) {
        userLikesService.deleteUserLikeById(id);
        return R.commonReturn(200, "取消点赞成功", "");
    }
    
    /**
     * 根据关联类型和关联ID取消点赞
     * @param type 关联类型（1：教学活动；2:回复）
     * @param correlationId 关联的教学活动或回复id
     * @return
     */
    @ApiOperation(value = "根据关联类型和关联ID取消点赞")
    @PostMapping("/deleteUserLikeByTypeAndCorrelationId")
    public R deleteUserLikeByTypeAndCorrelationId(
            @RequestParam(value = "type") Integer type, 
            @RequestParam(value = "correlationId") Long correlationId) {
        System.out.println("接收到取消点赞请求: type=" + type + ", correlationId=" + correlationId);
        userLikesService.deleteUserLikeByTypeAndCorrelationId(type, correlationId);
        return R.commonReturn(200, "取消点赞成功", "");
    }
    
    /**
     * 提供另一种取消点赞的方式，使用请求体传参
     */
    @ApiOperation(value = "使用JSON请求体根据关联类型和关联ID取消点赞")
    @PostMapping("/deleteUserLikeByBody")
    public R deleteUserLikeByBody(@RequestBody LikeStateParam param) {
        System.out.println("接收到取消点赞请求(JSON): type=" + param.getType() + ", correlationId=" + param.getCorrelationId());
        userLikesService.deleteUserLikeByTypeAndCorrelationId(param.getType(), param.getCorrelationId());
        return R.commonReturn(200, "取消点赞成功", "");
    }

    /**
     * 当前用户是否已为该活动/回复点赞（根据用户id和活动id/回复id查询点赞信息）
     * @param param
     * @return
     */
    @ApiOperation(value = "查询点赞状态")
    @PostMapping("/getLikeState")
    public R<Boolean> getLikeState(@RequestBody LikeStateParam param) {
        boolean likeState = userLikesService.getLikeState(param);
        return R.ok(likeState);
    }
}
