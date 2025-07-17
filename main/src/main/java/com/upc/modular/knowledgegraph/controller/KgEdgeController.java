package com.upc.modular.knowledgegraph.controller;


import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.knowledgegraph.entity.KgEdge;
import com.upc.modular.knowledgegraph.entity.KgNode;
import com.upc.modular.knowledgegraph.param.KgEdgeSearchParam;
import com.upc.modular.knowledgegraph.service.IKgEdgeService;
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
@RequestMapping("/kg-edge")
@Api(tags = "节点关系管理")
public class KgEdgeController {

    @Resource
    private IKgEdgeService kgEdgeService;

    @ApiOperation(value = "创建节点关系")
    @PostMapping("/insertKgEdge")
    public R insertKgEdge(@RequestBody KgEdge kgEdge) {

        kgEdgeService.insertKgEdge(kgEdge);
        return R.commonReturn(200, "新增成功", "");
    }

    /**
     * 即断开两个节点间的连接
     * @param id
     * @return
     */
    @ApiOperation(value = "删除节点关系")
    @PostMapping("/deleteKgEdgeById")
    public R deleteKgEdgeById(@RequestParam Long id) {
        kgEdgeService.deleteKgEdgeById(id);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "查询节点关系")
    @PostMapping("/getKgEdgeById")
    public R getKgEdgeById(@RequestParam Long id) {
        if (id == null || id == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        KgEdge kgEdge = kgEdgeService.getById(id);
        return R.commonReturn(200, "查询成功", kgEdge);
    }

    @ApiOperation(value = "按条件查询节点关系")
    @PostMapping("/getKgEdgeByConditions")
    public R<List<KgEdge>> getKgEdgeByConditions(@RequestParam KgEdgeSearchParam param) {
        List<KgEdge> kgEdgeList = kgEdgeService.getKgEdgeByConditions(param);
        return R.ok(kgEdgeList);
    }
}
