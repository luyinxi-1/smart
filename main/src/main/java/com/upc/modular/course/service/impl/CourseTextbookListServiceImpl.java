package com.upc.modular.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.course.controller.param.CourseTextbookListReturnParam;
import com.upc.modular.course.controller.param.CourseTextbookListSearchParam;
import com.upc.modular.course.controller.param.CourseTextbookUpdateParam;
import com.upc.modular.course.entity.CourseTextbookList;
import com.upc.modular.course.mapper.CourseTextbookListMapper;
import com.upc.modular.course.service.ICourseTextbookListService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

/*    @Override
    public Boolean updateCourseTextbook(CourseTextbookList courseTextbookList) {
        if (ObjectUtils.isEmpty(courseTextbookList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.updateById(courseTextbookList);
    }*/
@Override
@Transactional // 关键！保证删除和新增操作的原子性
public Boolean updateCourseTextbookRelation(CourseTextbookUpdateParam param) {
    // 1. 参数校验
    if (param == null || param.getCourseId() == null || param.getTextbookIds() == null) {
        throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "课程ID或教材ID列表不能为空");
    }

    // 2. 【使用MyBatis-Plus】根据课程ID，删除该课程所有旧的教材关联关系
    // this.remove(...) 是 MyBatis-Plus IService 接口提供的方法
    // LambdaQueryWrapper 是 MyBatis-Plus 提供的类型安全的查询构造器
    this.baseMapper.physicalDeleteByCourseId(param.getCourseId());
    // 如果传入的教材列表为空，那么到此为止，效果就是清空所有关联，直接返回成功
    if (param.getTextbookIds().isEmpty()) {
        return true;
    }
    // 3. 构造新的关联关系列表
    List<CourseTextbookList> newList = param.getTextbookIds().stream()
            .map(textbookId -> {
                CourseTextbookList relation = new CourseTextbookList();
                relation.setCourseId(param.getCourseId());
                relation.setTextbookId(textbookId);
                return relation;
            })
            .collect(Collectors.toList());

    // 4. 【使用MyBatis-Plus】批量插入新的关联关系
    // this.saveBatch(...) 是 MyBatis-Plus IService 接口提供的批量保存方法，效率很高
    return this.saveBatch(newList);
}

    @Override
    public List<CourseTextbookListReturnParam> selectCourseTextbookList(CourseTextbookListSearchParam param) {
        if (ObjectUtils.isEmpty(param.getCourseId())) {
            param.setCourseId(0L);
        }
        return courseTextbookListMapper.selectCourseTextbookList(param.getCourseId());
    }
}
