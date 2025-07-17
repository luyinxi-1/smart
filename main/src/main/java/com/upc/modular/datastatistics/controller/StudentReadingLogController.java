package com.upc.modular.datastatistics.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.datastatistics.controller.param.OfflineReadingLogParam;
import com.upc.modular.datastatistics.controller.param.SyncReceiptDTO;
import com.upc.modular.datastatistics.service.IStudentReadingLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author la
 * @since 2025-07-12
 */
@RestController
@RequestMapping("/student-reading-log")
@Api(tags = "学生阅读记录")
public class StudentReadingLogController {
    @Autowired
    IStudentReadingLogService studentReadingLogService;

    @ApiOperation("接收离线阅读记录")
    @PostMapping("receiveOfflineReadingLog")
    public R<SyncReceiptDTO> receiveOfflineReadingLog(@RequestBody List<OfflineReadingLogParam> dtoList) {
        SyncReceiptDTO receipt = studentReadingLogService.saveAndReceipt(dtoList);
        return R.ok(receipt);
    }
}
