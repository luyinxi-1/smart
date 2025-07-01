package com.upc.modular.auth.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.controller.param.SysLogParam.SysLogPageReturnParam;
import com.upc.modular.auth.controller.param.SysLogParam.SysLogPageSearchParam;
import com.upc.modular.auth.entity.SysLog;
import com.upc.modular.auth.service.ISysLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@RestController
@RequestMapping("/sys-log")
@Api(tags = "系统日志")
public class SysLogController {

    @Autowired
    private ISysLogService sysLogService;
    @ApiOperation(value = "添加系统日志")
    @PostMapping("/insert")
    public R insert(@RequestBody SysLog sysLog) {
        boolean save = sysLogService.save(sysLog);
        if (save) {
            return R.commonReturn(200, "新增成功", "");
        }
        return R.commonReturn(400, "新增失败", "");
    }

    @ApiOperation(value = "删除系统日志")
    @DeleteMapping("/batchDelete")
    public R batchDelete(@RequestBody IdParam idParam) {
        boolean b = sysLogService.removeBatchByIds(idParam.getIdList());
        if (b) {
            return R.commonReturn(200, "删除成功", "");
        }
        return R.commonReturn(400, "删除失败", "");
    }

    @ApiOperation(value = "分页查询系统日志")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<SysLogPageReturnParam>> getPage(@RequestBody SysLogPageSearchParam param) {
        Page<SysLogPageReturnParam> page = sysLogService.getPage(param);
        PageBaseReturnParam<SysLogPageReturnParam> resultPage = PageBaseReturnParam.ok(page);
        return R.page(resultPage);
    }



}
