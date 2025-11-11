package com.upc.modular.homepage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.homepage.entity.HomePageNoticeReadStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-10-31
 */
@Mapper
public interface HomePageNoticeReadStatusMapper extends BaseMapper<HomePageNoticeReadStatus> {
    
    /**
     * 根据通知ID和用户ID查询阅读状态记录
     * @param noticeId 通知ID
     * @param userId 用户ID
     * @return 阅读状态记录
     */
    @Select("SELECT id, notice_id, user_id, read_status, read_time, create_time FROM home_page_notice_read_status WHERE notice_id = #{noticeId} AND user_id = #{userId}")
    HomePageNoticeReadStatus selectByNoticeIdAndUserId(@Param("noticeId") Long noticeId, @Param("userId") Long userId);
}