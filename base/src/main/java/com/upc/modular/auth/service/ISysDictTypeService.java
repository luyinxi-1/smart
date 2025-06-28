package com.upc.modular.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.controller.param.SysDictTypeParam.SysDictTypePageSearchParam;
import com.upc.modular.auth.entity.SysDictType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface ISysDictTypeService extends IService<SysDictType> {

    String checkDictTypeUnique(SysDictType param);

    void deleteDictTypeByIds(IdParam idParam);

    void updateDictType(SysDictType dict);

    Page<SysDictType> selectDictTypeList(SysDictTypePageSearchParam dictType);
}
