package com.upc.modular.homepage.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.homepage.entity.MainImageConfiguration;
import com.upc.modular.homepage.service.IMainImageConfigurationService;
import com.upc.modular.teacher.dto.TeacherInsertDto;
import com.upc.modular.teacher.entity.Teacher;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-08-11
 */
@RestController
@RequestMapping("/main-image-configuration")
@Api(tags = "主图配置")
public class MainImageConfigurationController {

    @Autowired
    private IMainImageConfigurationService mainImageConfigurationService;

    @ApiOperation(value = "新增主图配置")
    @PostMapping("/insert")
    public R<Boolean> insert(@RequestBody MainImageConfiguration param) {
        return R.ok(mainImageConfigurationService.insert(param));
    }

    @ApiOperation(value = "删除主图配置")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestBody IdParam idParam) {
        return R.ok(mainImageConfigurationService.batchDelete(idParam));
    }

    @ApiOperation(value = "修改主图配置")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody MainImageConfiguration param) {
        return R.ok(mainImageConfigurationService.updateConfiguration(param));
    }

    @ApiOperation(value = "查询主图配置")
    @PostMapping("/selectALlConfiguration")
    public R<List<MainImageConfiguration>> selectALlConfiguration() {
        return R.ok(mainImageConfigurationService.selectALlConfiguration());
    }
}
