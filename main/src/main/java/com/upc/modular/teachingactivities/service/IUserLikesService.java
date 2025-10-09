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
    
    /**
     * 通过关联类型和关联ID取消点赞
     * @param type 关联类型（1：教学活动；2:回复）
     * @param correlationId 关联的教学活动或回复id
     */
    void deleteUserLikeByTypeAndCorrelationId(Integer type, Long correlationId);

    boolean getLikeState(LikeStateParam param);
}
