package com.upc.modular.knowledgegraph.controller;


import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.knowledgegraph.entity.KgNode;
import com.upc.modular.knowledgegraph.param.KgNodeSearchParam;
import com.upc.modular.knowledgegraph.param.TextbookKnowledgeGraphReturnParam;
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

    @ApiOperation(value = "删除知识节点")
    @PostMapping("/deleteKgNodeById")
    public R deleteKgNodeById(@RequestParam Long id) {
        kgNodeService.deleteKgNodeById(id);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "创建知识节点")
    @PostMapping("/insertKgEdge")
    public R insertKgNode(@RequestBody KgNode kgNode) {
        if (kgNode == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        kgNodeService.save(kgNode);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "查询节点信息")
    @PostMapping("/getKgNodeById")
    public R getKgNodeById(@RequestParam Long id) {
        if (id == null || id == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        KgNode kgEdge = kgNodeService.getById(id);
        return R.commonReturn(200, "查询成功", kgEdge);
    }

    @ApiOperation(value = "修改节点信息")
    @PostMapping("/updateKgNodeById")
    public R updateKgNodeById(@RequestBody KgNode kgEdge) {
        kgNodeService.updateKgNodeById(kgEdge);
        return R.commonReturn(200, "修改成功", "");
    }


    @ApiOperation(value = "按条件查询节点信息")
    @PostMapping("/getKgNodeByConditions")
    public R<List<KgNode>> getKgNodeByConditions(@RequestBody KgNodeSearchParam param) {
        List<KgNode> result = kgNodeService.getKgNodeByConditions(param);
        return R.commonReturn(200, "查询成功", result);
    }
    
    @ApiOperation(value = "获取教材知识图谱信息")
    @GetMapping("/getTextbookKnowledgeGraph")
    public R<TextbookKnowledgeGraphReturnParam> getTextbookKnowledgeGraph(@RequestParam Long textbookId) {
        TextbookKnowledgeGraphReturnParam result = kgNodeService.getTextbookKnowledgeGraph(textbookId);
        return R.commonReturn(200, "查询成功", result);
    }

}