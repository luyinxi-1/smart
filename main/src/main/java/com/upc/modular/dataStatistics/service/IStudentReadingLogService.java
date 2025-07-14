package com.upc.modular.dataStatistics.service;

import com.upc.modular.dataStatistics.controller.param.OfflineReadingLogParam;
import com.upc.modular.dataStatistics.controller.param.SyncReceiptDTO;
import com.upc.modular.dataStatistics.entity.StudentReadingLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author la
 * @since 2025-07-12
 */
public interface IStudentReadingLogService extends IService<StudentReadingLog> {
    SyncReceiptDTO saveAndReceipt(List<OfflineReadingLogParam> dtoList);
}
