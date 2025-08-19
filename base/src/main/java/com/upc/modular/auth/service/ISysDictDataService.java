package com.upc.modular.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictItemParam.SysDictItemPageSearchParam;
import com.upc.modular.auth.controller.param.SysDictItemParam.SysDictItemSearchParam;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysDictData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface ISysDictDataService extends IService<SysDictData> {

    boolean insertDictItem(SysDictData dictItem);

    void deleteDictItemByIds(IdParam idParam);

    Page<SysDictData> getPage(SysDictItemPageSearchParam param);

    List<SysDictData> selectDictDataByDictType(SysDictItemSearchParam param);

    Boolean updateDictData(SysDictData dict);
}
