package com.upc.modular.institution.service;

import com.upc.common.responseparam.R;
import com.upc.modular.auth.param.InstitutionDto;
import com.upc.modular.institution.param.InstitutionSearchParam;
import com.upc.modular.institution.entity.Institution;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-01
 */
public interface IInstitutionService extends IService<Institution> {

    void deleteInstitutionByIds(List<Long> ids);

    void updateInstitutionById(Institution institution);

    R<List<InstitutionDto>> getInstitutionByConditions(InstitutionSearchParam param);
}
