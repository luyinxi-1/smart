package com.upc.modular.teachingActivities.service.impl;

import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.teachingActivities.entity.UserLikes;
import com.upc.modular.teachingActivities.mapper.UserLikesMapper;
import com.upc.modular.teachingActivities.service.IUserLikesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-07
 */
@Service
public class UserLikesServiceImpl extends ServiceImpl<UserLikesMapper, UserLikes> implements IUserLikesService {

    @Override
    public void insertUserLike(UserLikes userLikes) {
        if (userLikes == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        if (userLikes.getType() == null || userLikes.getCreator() == null || userLikes.getCorrelationId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        this.save(userLikes);
    }

    @Override
    public void deleteUserLikeById(Long id) {
        if (id == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        this.removeById(id);
    }

    @Override
    public void getLikeState(Long id) {

    }


}
