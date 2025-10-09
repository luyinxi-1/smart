package com.upc.modular.teachingactivities.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.teachingactivities.entity.UserLikes;
import com.upc.modular.teachingactivities.mapper.UserLikesMapper;
import com.upc.modular.teachingactivities.param.LikeStateParam;
import com.upc.modular.teachingactivities.service.IUserLikesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;

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
        if (userLikes.getType() == null || userLikes.getCorrelationId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        if (userLikes.getCreator() == null) {
            UserInfoToRedis userInfo = UserUtils.get();
            if (userInfo == null) {
                throw new BusinessException(BusinessErrorEnum.PLEASE_LOGIN);
            }
            userLikes.setCreator(userInfo.getId());
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

    /**
     * 通过关联类型和关联ID取消点赞
     * 与deleteUserLikeById保持一致，不检查当前登录用户
     */
    @Override
    public void deleteUserLikeByTypeAndCorrelationId(Integer type, Long correlationId) {
        if (type == null || correlationId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        
        // 直接根据type和correlationId删除点赞记录，不考虑创建者
        LambdaQueryWrapper<UserLikes> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserLikes::getType, type);
        queryWrapper.eq(UserLikes::getCorrelationId, correlationId);
        
        int rows = baseMapper.delete(queryWrapper);
        System.out.println("根据type=" + type + ", correlationId=" + correlationId + " 删除了 " + rows + " 条点赞记录");
    }

    @Override
    public boolean getLikeState(LikeStateParam param) {
        if (param == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        if (param.getType() == null || param.getCorrelationId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }

        UserInfoToRedis userInfo = UserUtils.get();
        if (userInfo == null) {
            throw new BusinessException(BusinessErrorEnum.PLEASE_LOGIN);
        }

        LambdaQueryWrapper<UserLikes> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserLikes::getType, param.getType());
        queryWrapper.eq(UserLikes::getCorrelationId, param.getCorrelationId());
        queryWrapper.eq(UserLikes::getCreator, userInfo.getId());

        UserLikes one = this.getOne(queryWrapper);
        if (one == null) {
            return false;
        }
        if (one != null) {
            return true;
        }

        return false;
    }


}
