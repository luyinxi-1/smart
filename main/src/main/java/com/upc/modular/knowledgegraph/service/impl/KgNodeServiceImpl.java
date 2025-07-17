package com.upc.modular.knowledgegraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.knowledgegraph.entity.KgNode;
import com.upc.modular.knowledgegraph.mapper.KgNodeMapper;
import com.upc.modular.knowledgegraph.param.KgNodeSearchParam;
import com.upc.modular.knowledgegraph.service.IKgNodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xth
 * @since 2025-07-17
 */
@Service
public class KgNodeServiceImpl extends ServiceImpl<KgNodeMapper, KgNode> implements IKgNodeService {

    @Override
    public void updatekgNodeById(KgNode kgEdge) {
        if (kgEdge == null || kgEdge.getId() == null || kgEdge.getId() == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }

        this.updateById(kgEdge);
    }

    @Override
    public void deletekgEdgeById(Long id) {
        if(id == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, " ID不能为空");
        }
        this.removeById(id);
    }

    @Override
    public List<KgNode> getkgNodeByConditions(KgNodeSearchParam param) {
        if ((param.getObjectId() != null && param.getObjectId() != 0L) && (param.getNodeType() == null || "".equals(param.getNodeType()))) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "根据业务表id查询时，节点类型不能为空");
        }
        LambdaQueryWrapper<KgNode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(param.getNodeName()), KgNode::getNodeName, param.getNodeName());
        queryWrapper.eq(StringUtils.isNotBlank(param.getNodeType()), KgNode::getNodeType, param.getNodeType());
        queryWrapper.eq(param.getObjectId() != null, KgNode::getObjectId, param.getObjectId());

        List<KgNode> kgNodeList = this.list(queryWrapper);

        return kgNodeList;
    }
}
