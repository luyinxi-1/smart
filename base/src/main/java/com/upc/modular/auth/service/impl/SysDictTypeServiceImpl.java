package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.controller.param.SysDictTypeParam.SysDictTypePageSearchParam;
import com.upc.modular.auth.entity.SysDictItem;
import com.upc.modular.auth.entity.SysDictType;
import com.upc.modular.auth.mapper.SysDictItemMapper;
import com.upc.modular.auth.mapper.SysDictTypeMapper;
import com.upc.modular.auth.service.ISysDictTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictTypeByIds(IdParam idParam) {
        List<Long> idList = idParam.getIdList();
        if (CollectionUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID 列表不能为空");
        }

        // 查询所有待删的字典类型
        List<SysDictType> dictTypes = dictTypeMapper.selectBatchIds(idList);
        for (SysDictType dictType : dictTypes) {
            String dictTypeCode = dictType.getDictTypeCode();


            if (StringUtils.isNotBlank(dictTypeCode)) {
                LambdaQueryWrapper<SysDictItem> itemWrapper = Wrappers.lambdaQuery(SysDictItem.class)
                        .eq(StringUtils.isNotBlank(dictTypeCode), SysDictItem::getDictTypeCode, dictTypeCode);
                dictItemMapper.delete(itemWrapper);
            }

            // 删除字典类型
            dictTypeMapper.deleteById(dictType.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictType(SysDictType dict) {
        SysDictType existing = dictTypeMapper.selectById(dict.getId());
        String oldCode = existing.getDictTypeCode();
        String newCode = dict.getDictTypeCode();

        // 如果编码发生变化，更新所有字典项的 code 字段
        if (!Objects.equals(oldCode, newCode)) {
            LambdaUpdateWrapper<SysDictItem> updateWrapper = Wrappers.lambdaUpdate(SysDictItem.class)
                    .eq(SysDictItem::getDictTypeCode, oldCode)
                    .set(SysDictItem::getDictTypeCode, newCode);
            dictItemMapper.update(null, updateWrapper);
        }

        // 更新字典类型
        dictTypeMapper.updateById(dict);
    }

    @Override
    public Page<SysDictType> selectDictTypeList(SysDictTypePageSearchParam dictType) {
        Page<SysDictType> page = new Page<>(dictType.getCurrent(), dictType.getSize());
        MyLambdaQueryWrapper<SysDictType> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ObjectUtils.isNotEmpty(dictType.getStatus()), SysDictType::getStatus, dictType.getStatus())
                .like(ObjectUtils.isNotEmpty(dictType.getDictTypeName()), SysDictType::getDictTypeName, dictType.getDictTypeName())
                .between(
                        ObjectUtils.isNotEmpty(dictType.getStartTime()) && ObjectUtils.isNotEmpty(dictType.getEndTime()),
                        SysDictType::getAddDatetime,
                        dictType.getStartTime(),
                        dictType.getEndTime()
                )
                .orderBy(true, dictType.getIsAsc() == 1, SysDictType::getId);
        return this.page(page, lambdaQueryWrapper);
    }


}
