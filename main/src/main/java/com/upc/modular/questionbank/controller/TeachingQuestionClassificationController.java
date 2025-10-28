package com.upc.modular.questionbank.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.questionbank.controller.param.TeachingQuestionClassificationSearchParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionClassificationReturnVo;
import com.upc.modular.questionbank.controller.param.TopLevelTeachingQuestionClassificationSearchParam;
import com.upc.modular.questionbank.entity.TeachingQuestionClassification;
import com.upc.modular.questionbank.service.ITeachingQuestionClassificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author la
 * @since 2025-08-12
 */
@RestController
@RequestMapping("/teaching-question-classification")
@Api(tags = "题目分类")
public class TeachingQuestionClassificationController {

    @Autowired
    private ITeachingQuestionClassificationService teachingQuestionClassificationService;
    
    /**
     * 构建带创建人姓名的分类树
     * @param list 分类列表
     * @return 树形结构的分类列表
     */
    private List<TeachingQuestionClassificationReturnVo> buildDictTreeWithCreator(List<TeachingQuestionClassificationReturnVo> list) {
        // 将分类按照父ID分组
        Map<Long, List<TeachingQuestionClassificationReturnVo>> map = list.stream()
                .collect(Collectors.groupingBy(TeachingQuestionClassificationReturnVo::getParentId));

        // 为每个分类设置子分类
        list.forEach(item -> item.setChildrenVo(map.get(item.getId())));

        // 找到所有根节点（假设根节点的父ID为0）
        List<TeachingQuestionClassificationReturnVo> roots = list.stream()
                .filter(item -> item.getParentId() == 0)
                .collect(Collectors.toList());
        return roots;
    }

    @PostMapping("/insertTeachingQuestionClassification")
    @ApiOperation("新增题目分类")
    public R<Void> insertTeachingQuestionClassification(@RequestBody TeachingQuestionClassification param){
        teachingQuestionClassificationService.insertTeachingQuestionClassification(param);
        return R.ok();
    }

    @PostMapping("/removeTeachingQuestionClassification")
    @ApiOperation("批量删除题目分类")
    public R<Void> removeTeachingQuestionClassification(@RequestParam List<Long> idList){
        teachingQuestionClassificationService.removeTeachingQuestionClassification(idList);
        return R.ok();
    }

    @PostMapping("/updateTeachingQuestionClassification")
    @ApiOperation("更新题目分类")
    public R<Boolean> updateTeachingQuestionClassification(@RequestBody TeachingQuestionClassification param){
        boolean result = teachingQuestionClassificationService.updateTeachingQuestionClassification(param);
        return R.ok(result);
    }

    @PostMapping("/selectTeachingQuestionClassificationParentIdList")
    @ApiOperation("获取题目分类的所有上级分类")
    public R<List<TeachingQuestionClassification>> selectTeachingQuestionClassificationParentIdList(@RequestParam Integer classificationGrade){
        List<TeachingQuestionClassification> list = teachingQuestionClassificationService.selectTeachingQuestionClassificationParentIdList(classificationGrade);
        return R.ok(list);
    }

    @PostMapping("/selectTeachingQuestionClassificationDownList")
    @ApiOperation("获取题目分类的下级分类")
    public R<List<TeachingQuestionClassification>> selectTeachingQuestionClassificationDownList(@RequestParam Long id){
        List<TeachingQuestionClassification> list = teachingQuestionClassificationService.selectTeachingQuestionClassificationDownList(id);
        return R.ok(list);
    }

    @PostMapping("/selectTeachingQuestionClassificationList")
    @ApiOperation("获取分类列表")
    public R<List<TeachingQuestionClassificationReturnVo>> selectTeachingQuestionClassificationList(@RequestBody TeachingQuestionClassificationSearchParam param){
        List<TeachingQuestionClassificationReturnVo> list = ((com.upc.modular.questionbank.service.impl.TeachingQuestionClassificationServiceImpl) teachingQuestionClassificationService).selectTeachingQuestionClassificationListWithCreator(param);
        List<TeachingQuestionClassificationReturnVo> teachingQuestionClassification = buildDictTreeWithCreator(list);
        return R.ok(teachingQuestionClassification);
    }

    @PostMapping("/updateTeachingQuestionClassificationSortName")
    @ApiOperation("更改题目分类排序(0向上，1向下)")
    public R<Boolean> updateTeachingQuestionClassificationSortName(@RequestParam Long id, @RequestParam Integer param){
        boolean result = teachingQuestionClassificationService.updateTeachingQuestionClassificationSortName(id, param);
        return R.ok(result);
    }

    @GetMapping("selectTopLevelTeachingQuestionClassification")
    @ApiOperation("查询顶级题目分类")
// 移除 @RequestBody 和参数对象
    public R<List<TeachingQuestionClassification>> selectTopLevelTeachingQuestionClassification() {
        List<TeachingQuestionClassification> list = teachingQuestionClassificationService.selectTopLevelTeachingQuestionClassification();
        return R.ok(list);
    }
}
