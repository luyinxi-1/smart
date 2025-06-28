package com.upc.modular.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictItemParam.SysDictItemPageSearchParam;
import com.upc.modular.auth.controller.param.SysDictItemParam.SysDictItemSearchParam;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysDictItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.auth.entity.SysDictType;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface ISysDictItemService extends IService<SysDictItem> {

    boolean insertDictItem(SysDictItem dictItem);

    void deleteDictItemByIds(IdParam idParam);

    Page<SysDictItem> getPage(SysDictItemPageSearchParam param);

    List<SysDictItem> selectDictDataByDictType(SysDictItemSearchParam param);
}
