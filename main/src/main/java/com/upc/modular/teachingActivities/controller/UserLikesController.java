package com.upc.modular.teachingActivities.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.teachingActivities.entity.DiscussionTopic;
import com.upc.modular.teachingActivities.entity.UserLikes;
import com.upc.modular.teachingActivities.service.IUserLikesService;
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
        return R.commonReturn(200, "新增成功", "");
    }

    /**
     * 当前用户是否已为该活动/回复点赞（根据用户id和活动id/回复id查询点赞信息）
     * @param id
     * @return
     */
    @ApiOperation(value = "")
    @PostMapping("/getLikeState")
    public R getLikeState(@RequestParam Long id) {
        userLikesService.getLikeState(id);
        return R.commonReturn(200, "新增成功", "");
    }
}
