package com.upc.modular.homepage.mapper;

import com.upc.modular.homepage.entity.HomePageNotice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.homepage.param.HomePageNoticeReturnParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-07-15
 */
@Mapper
public interface HomePageNoticeMapper extends BaseMapper<HomePageNotice> {

    HomePageNoticeReturnParam getHomePageNoticeDetails(@Param("noticeId")Long noticeId);
}
