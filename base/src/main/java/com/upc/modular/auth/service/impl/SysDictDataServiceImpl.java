package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictItemParam.SysDictItemPageSearchParam;
import com.upc.modular.auth.controller.param.SysDictItemParam.SysDictItemSearchParam;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysDictData;
import com.upc.modular.auth.mapper.SysDictItemMapper;
import com.upc.modular.auth.service.ISysDictDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictData> implements ISysDictDataService {

    @Autowired
    private SysDictItemMapper sysDictItemMapper;
    @Override
    public boolean insertDictItem(SysDictData dictItem) {
        MyLambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(ObjectUtils.isNotEmpty(dictItem.getDictType()), SysDictData::getDictType, dictItem.getDictType());

        if (ObjectUtils.isNotEmpty(dictItem.getDictKey()) || ObjectUtils.isNotEmpty(dictItem.getName())) {
            lambdaQueryWrapper.and(w -> w
                    .eq(ObjectUtils.isNotEmpty(dictItem.getDictKey()), SysDictData::getDictKey, dictItem.getDictKey())
                    .or()
                    .eq(ObjectUtils.isNotEmpty(dictItem.getName()), SysDictData::getName, dictItem.getName())
            );
        }
        List<SysDictData> sysDictData = sysDictItemMapper.selectList(lambdaQueryWrapper);
        if (ObjectUtils.isNotEmpty(sysDictData)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "新增字典内容'" + dictItem.getDictKey() + "'失败，字典内容value值已存在");
        }
        sysDictItemMapper.insert(dictItem);
        return true;
    }

    @Override
    public void deleteDictItemByIds(IdParam idParam) {
        List<Long> idList = idParam.getIdList();
        if (CollectionUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID 列表不能为空");
        }
        for (Long id : idList) {
            sysDictItemMapper.deleteById(id);
        }
    }

    @Override
    public Page<SysDictData> getPage(SysDictItemPageSearchParam param) {
        Page<SysDictData> page = new Page<>(param.getCurrent(), param.getSize());

        MyLambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();

        lambdaQueryWrapper
                .eq(SysDictData::getDictType, param.getDictType())
                .like(ObjectUtils.isNotEmpty(param.getName()), SysDictData::getName, param.getName())
                .eq(ObjectUtils.isNotEmpty(param.getStatus()), SysDictData::getStatus, param.getStatus());
        return this.page(page, lambdaQueryWrapper);
    }

    @Override
    public List<SysDictData> selectDictDataByDictType(SysDictItemSearchParam param) {
        MyLambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ObjectUtils.isNotEmpty(param.getDictType()), SysDictData::getDictType, param.getDictType())
                .like(ObjectUtils.isNotEmpty(param.getName()), SysDictData::getName, param.getName());
        return sysDictItemMapper.selectList(lambdaQueryWrapper);
    }
}
