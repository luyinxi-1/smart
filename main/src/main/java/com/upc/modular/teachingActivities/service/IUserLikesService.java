package com.upc.modular.teachingActivities.service;

import com.upc.modular.teachingActivities.entity.UserLikes;
import com.baomidou.mybatisplus.extension.service.IService;

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

    void getLikeState(Long id);
}
