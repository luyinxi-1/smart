package com.upc.modular.textbook.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.LearningAnnotationsAndLabels;
import com.upc.modular.textbook.service.ILearningAnnotationsAndLabelsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author la
 * @since 2025-07-15
 */
@RestController
@RequestMapping("/learning-annotations-and-labels")
@Api(tags = "学习批注和标注")
public class LearningAnnotationsAndLabelsController {

    @Autowired
    private ILearningAnnotationsAndLabelsService learningAnnotationsAndLabelsService;

    @ApiOperation(value = "删除学习标注和批注")
    @DeleteMapping("/batchDetele")
    public R<Boolean> batchDetele(@RequestBody IdParam idParam) {
        return R.ok(learningAnnotationsAndLabelsService.batchDetele(idParam));
    }

    @ApiOperation(value = "修改学习标注和批注")
    @PutMapping("/saveOrUpdateLabels")
    public R<Boolean> saveOrUpdateLabels(@RequestBody LearningAnnotationsAndLabels param) {
        return R.ok(learningAnnotationsAndLabelsService.saveOrUpdateLabels(param));
    }
}
