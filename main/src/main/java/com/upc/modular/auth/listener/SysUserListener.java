package com.upc.modular.auth.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.param.SysUserImportParam;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.auth.service.impl.SysUserServiceImpl;
import com.upc.modular.auth.utils.AgeQuantifyUtils;
import com.upc.modular.auth.utils.TypeConversionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class SysUserListener extends AnalysisEventListener<SysUserImportParam> {

    private static final int BATCH_COUNT = 1000;

    private final ISysUserService sysUserService;

    private final Map<String, SysTbuser> existSysUserMap;

    /**
     * 新增数据列表
     */
    List<SysTbuser> sysUserList = new ArrayList<>(BATCH_COUNT);
    /**
     * 更新数据列表
     */
    List<SysTbuser> updateSysUserList = new ArrayList<>(BATCH_COUNT);

    /**
     * 新增数据条数
     */
    @Getter
    private long insertTotal;

    /**
     * 更新数据条数
     */
    @Getter
    private long updateTotal;

    public SysUserListener(ISysUserService sysUserService, List<SysTbuser> existSysUserList) {
        this.sysUserService= sysUserService;
        this.existSysUserMap = existSysUserList.stream()
                .filter(user -> StringUtils.isNotBlank(user.getIdcard()))
                .collect(Collectors.toMap(
                        SysTbuser::getIdcard,
                        Function.identity(), // value 就是这个 user 本身
                        (oldVal, newVal) -> oldVal // 如果身份证重复，保留第一个
                ));
    }
    @Override
    public void invoke(SysUserImportParam sysUserImportParam, AnalysisContext analysisContext) {
        String idcard = sysUserImportParam.getIdcard();
        if (StringUtils.isBlank(idcard) || idcard.length() < 14) {
            log.warn("跳过无效数据，身份证号格式错误：{}", idcard);
            return; // 或者记录到错误列表中
        }
        SysTbuser sysTbuser = new SysTbuser();
        BeanUtils.copyProperties(sysUserImportParam, sysTbuser);
        String dateBirth = AgeQuantifyUtils.getBirthDateFromIdNumber(sysUserImportParam.getIdcard());
        int age = AgeQuantifyUtils.getAgeFromIdNumber(sysUserImportParam.getIdcard());
        String newGender = TypeConversionUtils.sexToString(AgeQuantifyUtils.getGenderFromIdNumber(sysUserImportParam.getIdcard()));

        sysTbuser.setBirthday(dateBirth);
        sysTbuser.setAge(age);
        sysTbuser.setGender(newGender);
        sysTbuser.setStatus(0);

        // 用 map 判断是否存在
        SysTbuser existUser = existSysUserMap.get(sysTbuser.getIdcard());

        if (existUser != null) {
            sysTbuser.setId(existUser.getId()); // 设置 ID 用于更新
            updateSysUserList.add(sysTbuser);
            updateTotal++;
        } else {
            sysUserList.add(sysTbuser);
            insertTotal++;
        }

        if (sysUserList.size() >= BATCH_COUNT || updateSysUserList.size() >= BATCH_COUNT) {
            this.saveDataAsync();
            sysUserList.clear();
            updateSysUserList.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        this.saveDataAsync();
        if (insertTotal > 3000) {
            log.warn("导入总数为：{}", insertTotal);
        } else {
            log.info("导入总数为：{}", insertTotal);
        }
    }

    private void saveDataAsync() {
        if (CollectionUtils.isNotEmpty(sysUserList)) {
            sysUserService.saveBatch(sysUserList);
        }
        if (CollectionUtils.isNotEmpty(updateSysUserList)){
            sysUserService.updateBatchById(updateSysUserList);
        }
        log.info("导入成功");
    }
}
