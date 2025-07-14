package com.upc.modular.offlineLearning.service.impl;

import com.upc.modular.dataStatistics.mapper.StudentReadingLogMapper;
import com.upc.modular.offlineLearning.controller.param.SyncRequestDTO;
import com.upc.modular.offlineLearning.controller.param.SyncResponseDTO;
import com.upc.modular.offlineLearning.entity.OfflineReadingLog;
import com.upc.modular.offlineLearning.mapper.OfflineReadingLogMapper;
import com.upc.modular.offlineLearning.service.IOfflineReadingLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author la
 * @since 2025-07-12
 */
@Service
public class OfflineReadingLogServiceImpl extends ServiceImpl<OfflineReadingLogMapper, OfflineReadingLog> implements IOfflineReadingLogService {

}
