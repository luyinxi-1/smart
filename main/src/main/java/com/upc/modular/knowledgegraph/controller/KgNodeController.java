package com.upc.modular.knowledgegraph.controller;


import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.institution.entity.Institution;
import com.upc.modular.knowledgegraph.entity.KgEdge;
import com.upc.modular.knowledgegraph.entity.KgNode;
import com.upc.modular.knowledgegraph.param.KgNodeSearchParam;
import com.upc.modular.knowledgegraph.service.IKgNodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xth
 * @since 2025-07-17
 */
@RestController
@RequestMapping("/kg-node")
@Api(tags = "节点管理")
public class KgNodeController {

    @Resource
    private IKgNodeService kgNodeService;

    @ApiOperation(value = "删除机构")
    @PostMapping("/deletekgEdgeById")
    public R deletekgEdgeById(@RequestParam Long id) {
        kgNodeService.deletekgEdgeById(id);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "创建知识节点")
    @PostMapping("/insertkgEdge")
    public R insertkgNode(@RequestBody KgNode kgEdge) {
        if (kgEdge == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        kgNodeService.save(kgEdge);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "查询机构信息")
    @PostMapping("/getkgNodeById")
    public R getkgNodeById(@RequestParam Long id) {
        if (id == null || id == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        KgNode kgEdge = kgNodeService.getById(id);
        return R.commonReturn(200, "查询成功", kgEdge);
    }

    @ApiOperation(value = "修改节点信息")
    @PostMapping("/updatekgNodeById")
    public R updatekgNodeById(@RequestBody KgNode kgEdge) {
        kgNodeService.updatekgNodeById(kgEdge);
        return R.commonReturn(200, "修改成功", "");
    }


    @ApiOperation(value = "按条件查询机构信息")
    @PostMapping("/getkgNodeByConditions")
    public R<List<KgNode>> getkgNodeByConditions(@RequestBody KgNodeSearchParam param) {
        List<KgNode> result = kgNodeService.getkgNodeByConditions(param);
        return R.commonReturn(200, "查询成功", result);
    }

}
