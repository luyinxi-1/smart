package com.upc.modular.homepage.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.homepage.entity.HomePageNotice;
import com.upc.modular.homepage.mapper.HomePageNoticeMapper;
import com.upc.modular.homepage.param.HomePageNoticeClassListParam;
import com.upc.modular.homepage.param.HomePageNoticeListSearchParam;
import com.upc.modular.homepage.param.HomePageNoticePageSearchParam;
import com.upc.modular.homepage.param.HomePageNoticeReturnParam;
import com.upc.modular.homepage.service.IHomePageNoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-15
 */
@Service
public class HomePageNoticeServiceImpl extends ServiceImpl<HomePageNoticeMapper, HomePageNotice> implements IHomePageNoticeService {

    @Autowired
    private HomePageNoticeMapper homePageNoticeMapper;

    @Override
    public Boolean insert(HomePageNotice homePageNotice) {
        if (ObjectUtils.isEmpty(homePageNotice)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        if (ObjectUtils.isEmpty(homePageNotice.getIsTop())) {
            homePageNotice.setIsTop(0);
        }
        return this.save(homePageNotice);
    }

    @Override
    public Boolean batchDelete(List<Long> idList) {
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.removeBatchByIds(idList);
    }

    @Override
    public Boolean updateNotice(HomePageNotice homePageNotice) {
        if (ObjectUtils.isEmpty(homePageNotice)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.updateById(homePageNotice);
    }

    @Override
    public List<HomePageNoticeReturnParam> getHomePageNotice(HomePageNoticeListSearchParam param) {
        return homePageNoticeMapper.selectNoticeListWithNames(param);
    }

    @Override
    public Page<HomePageNoticeReturnParam> getHomePageNoticePage(HomePageNoticePageSearchParam param) {
        Page<HomePageNoticeReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        return homePageNoticeMapper.selectNoticePageWithNames(page, param);
    }

    @Override
    public HomePageNoticeReturnParam getHomePageNoticeDetails(Long noticeId) {
        return homePageNoticeMapper.getHomePageNoticeDetails(noticeId);
    }

    @Override
    public Boolean insertTextbookNotice(HomePageNoticeClassListParam homePageNotice) {
        return null;
    }


}
