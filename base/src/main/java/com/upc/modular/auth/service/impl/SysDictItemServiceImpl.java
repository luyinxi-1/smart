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
import com.upc.modular.auth.entity.SysDictItem;
import com.upc.modular.auth.entity.SysDictType;
import com.upc.modular.auth.mapper.SysDictItemMapper;
import com.upc.modular.auth.service.ISysDictItemService;
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
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements ISysDictItemService {

    @Autowired
    private SysDictItemMapper sysDictItemMapper;
    @Override
    public boolean insertDictItem(SysDictItem dictItem) {
        MyLambdaQueryWrapper<SysDictItem> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(ObjectUtils.isNotEmpty(dictItem.getDictTypeCode()), SysDictItem::getDictTypeCode, dictItem.getDictTypeCode());

        if (ObjectUtils.isNotEmpty(dictItem.getDictItemValue()) || ObjectUtils.isNotEmpty(dictItem.getDictItemName())) {
            lambdaQueryWrapper.and(w -> w
                    .eq(ObjectUtils.isNotEmpty(dictItem.getDictItemValue()), SysDictItem::getDictItemValue, dictItem.getDictItemValue())
                    .or()
                    .eq(ObjectUtils.isNotEmpty(dictItem.getDictItemName()), SysDictItem::getDictItemName, dictItem.getDictItemName())
            );
        }
        List<SysDictItem> sysDictItems = sysDictItemMapper.selectList(lambdaQueryWrapper);
        if (ObjectUtils.isNotEmpty(sysDictItems)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "新增字典内容'" + dictItem.getDictItemValue() + "'失败，字典内容value值已存在");
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
    public Page<SysDictItem> getPage(SysDictItemPageSearchParam param) {
        Page<SysDictItem> page = new Page<>(param.getCurrent(), param.getSize());

        MyLambdaQueryWrapper<SysDictItem> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();

        lambdaQueryWrapper
                .eq(SysDictItem::getDictTypeCode, param.getDictTypeCode())
                .like(ObjectUtils.isNotEmpty(param.getDictItemName()), SysDictItem::getDictItemName, param.getDictItemName())
                .eq(ObjectUtils.isNotEmpty(param.getStatus()), SysDictItem::getStatus, param.getStatus())
                .orderByAsc(SysDictItem::getItemOrder);  // 按 item_order 升序排列

        return this.page(page, lambdaQueryWrapper);
    }

    @Override
    public List<SysDictItem> selectDictDataByDictType(SysDictItemSearchParam param) {
        MyLambdaQueryWrapper<SysDictItem> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysDictItem::getDictTypeCode, param.getDictTypeCode())
                .like(ObjectUtils.isNotEmpty(param.getDictItemName()), SysDictItem::getDictItemName, param.getDictItemName());
        return sysDictItemMapper.selectList(lambdaQueryWrapper);
    }
}
