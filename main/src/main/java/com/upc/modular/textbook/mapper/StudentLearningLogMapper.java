package com.upc.modular.textbook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.textbook.entity.StudentLearningLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentLearningLogMapper extends BaseMapper<StudentLearningLog> {
}