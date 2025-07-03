package com.upc.modular.course.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.course.controller.param.CourseDataExportParam;
import com.upc.modular.course.controller.param.CourseDataExportSearchParam;
import com.upc.modular.course.controller.param.CoursePageReturnParam;
import com.upc.modular.course.controller.param.CoursePageSearchParam;
import com.upc.modular.course.entity.Course;
import com.upc.modular.course.mapper.CourseMapper;
import com.upc.modular.course.service.ICourseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
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
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    @Autowired
    CourseMapper courseMapper;
    @Override
    public Void deleteCourseByIds(IdParam idParam) {
        List<Long> idList = idParam.getIdList();
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }

        // 查询课程记录
        List<Course> courses = courseMapper.selectBatchIds(idList);
        if (ObjectUtils.isEmpty(courses)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "未找到对应的课程记录");
        }
        this.removeByIds(idList);

        return null;
    }

    @Override
    public Page<CoursePageReturnParam> getPage(CoursePageSearchParam param) {
        Page<CoursePageReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        return courseMapper.selectCourse(page, param);
    }

    @Override
    public void exportCourseData(HttpServletResponse response, IdParam param) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");//直接用
        response.setCharacterEncoding("utf-8");//直接用
        try {
            //下面四行直接复制，只需要改“课程信息表”这里
            String fileName = URLEncoder.encode("课程信息表", "UTF-8").
                    replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''"
                    + fileName + ".xlsx");
            // 查询出要返回的
            List<Long> idList = param.getIdList();
            List<Course> courseList = courseMapper.selectList(
                    new LambdaQueryWrapper<Course>()
                            .in(ObjectUtils.isNotNull(idList), Course::getId, idList)
            );
            List<CourseDataExportParam> exportList = courseList.stream()
                    .map(course -> {
                        CourseDataExportParam dto = new CourseDataExportParam();
                        // 复制字段
                        dto.setTextbookId(course.getTextbookId())
                                .setCourseName(course.getCourseName())
                                .setCredit(course.getCredit())
                                .setDescription(course.getDescription());
                        // status 转换
                        Integer st = course.getStatus();
                        dto.setStatus(st == null ? "未知状态"
                                : st == 0   ? "未发布"
                                : st == 1   ? "已发布"
                                : "其它(" + st + ")");
                        return dto;
                    })
                    .collect(Collectors.toList());
            //TODO
            /**
             * 这里需要等教材表建完之后，把exportList中的教材id变为教材名称。然后把CourseDataExportParam里面的教材id字段改成教材名称。
             */

            EasyExcel.write(response.getOutputStream(), CourseDataExportParam.class)
                    .sheet("课程列表")
                    .doWrite(exportList);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("导出失败，请重试");
        }
    }
}
