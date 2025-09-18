package com.upc.modular.course.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.course.controller.param.CourseTextbookListReturnParam;
import com.upc.modular.course.controller.param.CourseTextbookListSearchParam;
import com.upc.modular.course.controller.param.CourseTextbookUpdateParam;
import com.upc.modular.course.entity.CourseTextbookList;
import com.upc.modular.course.service.ICourseClassListService;
import com.upc.modular.course.service.ICourseTextbookListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@RestController
@RequestMapping("/course-textbook-list")
@Api(tags = "课程教材关联")
public class CourseTextbookListController {
    @Autowired
    private ICourseTextbookListService courseTextbookListService;

    @ApiOperation(value = "新增课程教材关联")
    @PostMapping("/insert")
    public R<Boolean> insert(@RequestBody CourseTextbookList courseTextbookList) {
        return R.ok(courseTextbookListService.insert(courseTextbookList));
    }

    @ApiOperation(value = "删除教材课程关联")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestParam("idList") List<Long> idList) {
        return R.ok(courseTextbookListService.batchDelete(idList));
    }

/*
    @ApiOperation(value = "更新教材课程关联")
    @PostMapping("/updateCourseTextbook")
    public R<Boolean> updateCourseTextbook(@RequestBody CourseTextbookList courseTextbookList) {
        return R.ok(courseTextbookListService.updateCourseTextbook(courseTextbookList));
    }

*/
@ApiOperation(value = "更新课程与教材的关联关系")
@PostMapping("/updateCourseTextbook")
public R<Boolean> updateCourseTextbook(@RequestBody CourseTextbookUpdateParam param) {
    // 将接收到的参数传递给 Service 层进行处理
    return R.ok(courseTextbookListService.updateCourseTextbookRelation(param));
}


    @ApiOperation(value = "查询教材课程绑定关系")
    @PostMapping("/selectCourseTextbookList")
    public R<List<CourseTextbookListReturnParam>> selectCourseTextbookList(@RequestBody CourseTextbookListSearchParam param) {
        return R.ok(courseTextbookListService.selectCourseTextbookList(param));
    }
}
