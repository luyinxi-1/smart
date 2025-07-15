package com.upc.modular.teachingactivities.service;

import com.upc.modular.teachingactivities.entity.UserLikes;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.teachingactivities.param.LikeStateParam;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-07
 */
public interface IUserLikesService extends IService<UserLikes> {

    void insertUserLike(UserLikes userLikes);

    void deleteUserLikeById(Long id);

    boolean getLikeState(LikeStateParam param);
}
