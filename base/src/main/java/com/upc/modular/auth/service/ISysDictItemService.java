package com.upc.modular.auth.service;

import com.upc.modular.auth.entity.SysDictItem;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
