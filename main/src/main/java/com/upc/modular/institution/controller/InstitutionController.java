package com.upc.modular.institution.controller;


import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.param.InstitutionDto;
import com.upc.modular.institution.Param.InstitutionSearchParam;
import com.upc.modular.institution.entity.Institution;
import com.upc.modular.institution.service.IInstitutionService;
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
 * @since 2025-07-01
 */
@RestController
@RequestMapping("/institution")
@Api(tags = "机构管理")
public class InstitutionController {

    @Autowired
    private IInstitutionService institutionService;

    @ApiOperation(value = "删除机构")
    @PostMapping("/deleteInstitutionByIds")
    public R deleteInstitutionByIds(@RequestBody List<Long> ids) {
        institutionService.deleteInstitutionByIds(ids);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "新增机构")
    @PostMapping("/insertInstitution")
    public R insertInstitution(@RequestBody Institution institution) {
        if (institution == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        institutionService.save(institution);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "查询机构信息")
    @PostMapping("/getInstitutionById")
    public R getInstitutionById(@RequestParam Long id) {
        if (id == null || id == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        Institution institution = institutionService.getById(id);
        return R.commonReturn(200, "查询成功", institution);
    }

    @ApiOperation(value = "修改机构信息")
    @PostMapping("/updateInstitutionById")
    public R updateInstitutionById(@RequestBody Institution institution) {
        institutionService.updateInstitutionById(institution);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "按条件查询机构信息")
    @PostMapping("/getInstitutionByConditions")
    public R<List<InstitutionDto>> getInstitutionByConditions(@RequestBody InstitutionSearchParam param) {
        return institutionService.getInstitutionByConditions(param);
    }

}
