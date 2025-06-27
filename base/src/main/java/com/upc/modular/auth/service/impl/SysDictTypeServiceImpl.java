package com.upc.modular.auth.service.impl;

import com.upc.modular.auth.entity.SysDictType;
import com.upc.modular.auth.mapper.SysDictItemMapper;
import com.upc.modular.auth.mapper.SysDictTypeMapper;
import com.upc.modular.auth.service.ISysDictTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {

    @Autowired
    private SysDictTypeMapper dictTypeMapper;

    @Autowired
    private SysDictItemMapper dictItemMapper;
    @Override
    public String checkDictTypeUnique(SysDictType dict) {
        Long dictId = dict.getId() == null ? 0 : dict.getId();
        SysDictType dictType = dictTypeMapper.checkDictTypeUnique(dict.getDictTypeName());
        if (dictType != null && !Objects.equals(dictType.getId(), dictId)) {
            return "0";
        }
        return "1";
    }
}
