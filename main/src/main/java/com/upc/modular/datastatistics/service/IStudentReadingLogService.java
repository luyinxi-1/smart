package com.upc.modular.datastatistics.service;

import com.upc.modular.datastatistics.controller.param.OfflineReadingLogParam;
import com.upc.modular.datastatistics.controller.param.SyncReceiptDTO;
import com.upc.modular.datastatistics.entity.StudentReadingLog;
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
