package com.upc.modular.course.service;

import com.upc.modular.course.controller.param.CourseTextbookListReturnParam;
import com.upc.modular.course.controller.param.CourseTextbookListSearchParam;
import com.upc.modular.course.controller.param.CourseTextbookUpdateParam;
import com.upc.modular.course.entity.CourseTextbookList;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface ICourseTextbookListService extends IService<CourseTextbookList> {

    Boolean insert(CourseTextbookList courseTextbookList);

    Boolean batchDelete(List<Long> idList);
    /**
     * 更新课程与教材的关联关系（先删后增）
     * @param param 包含课程ID和教材ID列表的参数
     * @return 操作是否成功
     */
    Boolean updateCourseTextbookRelation(CourseTextbookUpdateParam param);
    //Boolean updateCourseTextbook(CourseTextbookList courseTextbookList);
    List<CourseTextbookListReturnParam> selectCourseTextbookList(CourseTextbookListSearchParam param);
}
