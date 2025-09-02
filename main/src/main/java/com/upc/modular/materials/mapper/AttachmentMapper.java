package com.upc.modular.materials.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.auth.entity.RoleAuthorityList;
import com.upc.modular.materials.entity.Attachment;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: xth
 * @Date: 2025/9/2 15:14
 */
@Mapper
public interface AttachmentMapper extends BaseMapper<Attachment> {
}
