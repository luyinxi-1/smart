package com.upc.modular.course.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.course.controller.param.CourseTextbookListReturnParam;
import com.upc.modular.course.controller.param.CourseTextbookListSearchParam;
import com.upc.modular.course.entity.CourseTextbookList;
import com.upc.modular.course.mapper.CourseTextbookListMapper;
import com.upc.modular.course.service.ICourseTextbookListService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Service
public class CourseTextbookListServiceImpl extends ServiceImpl<CourseTextbookListMapper, CourseTextbookList> implements ICourseTextbookListService {

    @Autowired
    private CourseTextbookListMapper courseTextbookListMapper;
    @Override
    public Boolean insert(CourseTextbookList courseTextbookList) {
        if (ObjectUtils.isEmpty(courseTextbookList) || ObjectUtils.isEmpty(courseTextbookList.getCourseId()) || ObjectUtils.isEmpty(courseTextbookList.getTextbookId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.save(courseTextbookList);
    }

    @Override
    public Boolean batchDelete(List<Long> idList) {
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.removeBatchByIds(idList);
    }

    @Override
    public Boolean updateCourseTextbook(CourseTextbookList courseTextbookList) {
        if (ObjectUtils.isEmpty(courseTextbookList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.updateById(courseTextbookList);
    }

    @Override
    public List<CourseTextbookListReturnParam> selectCourseTextbookList(CourseTextbookListSearchParam param) {
        if (ObjectUtils.isEmpty(param.getCourseId())) {
            param.setCourseId(0L);
        }
        return courseTextbookListMapper.selectCourseTextbookList(param.getCourseId());
    }
}
