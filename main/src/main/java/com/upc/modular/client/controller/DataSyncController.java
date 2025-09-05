package com.upc.modular.client.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.common.responseparam.R;
import com.upc.common.utils.UserUtils;
import com.upc.modular.client.controller.param.SyncInfoReturnParam;
import com.upc.modular.client.entity.ClientLearningLog;
import com.upc.modular.textbook.entity.LearningLog;
import com.upc.modular.textbook.service.impl.LearningLogServiceImpl;
import io.swagger.annotations.Api;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "数据同步接口")
@RestController
@RequestMapping("/dataSync")
public class DataSyncController {

    @Autowired
    private LearningLogServiceImpl learningLogService;

    @PostMapping("/learningLog")
    public R<SyncInfoReturnParam> learningLog(@RequestBody ArrayList<ClientLearningLog> clientLearningLogs) {

        System.out.println("clientLearningLogs: " + clientLearningLogs);
        return R.ok(syncData(
                clientLearningLogs,
                learningLogService,
                ClientLearningLog::getClientUuid,
                LearningLog::getClientUuid,
                ClientLearningLog::getId,
                LearningLog::getId,
                ClientLearningLog::getIsDelete,
                LearningLog.class
        ));

//        Map<String, com.upc.modular.client.entity.LearningLog> clientUuidMap = clientLearningLogs.stream()
//                .collect(Collectors.toMap(com.upc.modular.client.entity.LearningLog::getClientUuid, log -> log, (log1, log2) -> log1));
//
//        Map<String, LearningLog> serverUuidMap = learningLogService.list(
//                        new LambdaQueryWrapper<LearningLog>()
//                                .in(LearningLog::getClientUuid, clientUuidMap.keySet()))
//                .stream()
//                .collect(Collectors.toMap(LearningLog::getClientUuid, log -> log, (log1, log2) -> log1));
//
//        List<LearningLog> newLearningLogs = new ArrayList<>();
//        List<LearningLog> updateLearningLogs = new ArrayList<>();
//        List<LearningLog> deleteLearningLogs = new ArrayList<>();
//
//        for (com.upc.modular.client.entity.LearningLog clientLearningLog : clientLearningLogs) {
//            LearningLog serverLearningLog = serverUuidMap.get(clientLearningLog.getClientUuid());
//
//            if (serverLearningLog == null) {
//                // 不存在 → 新增
//                LearningLog temp = new LearningLog();
//                BeanUtils.copyProperties(clientLearningLog, temp);
//                temp.setId(null);
//                newLearningLogs.add(temp);
//            } else if (clientLearningLog.getIsDelete() == 1) {
//                // 存在且被删除 → 删除
//                deleteLearningLogs.add(serverLearningLog);
//            } else {
//                // 存在且未删除 → 修改
//                LearningLog temp = new LearningLog();
//                BeanUtils.copyProperties(clientLearningLog, temp);
//                temp.setId(serverLearningLog.getId());
//                updateLearningLogs.add(temp);
//            }
//        }
//
//        // 批量执行
//        boolean saveBatch = newLearningLogs.isEmpty() || learningLogService.saveBatch(newLearningLogs);
//        boolean updateBatch = updateLearningLogs.isEmpty() || learningLogService.updateBatchById(updateLearningLogs);
//        boolean deleteBatch = deleteLearningLogs.isEmpty() || learningLogService.removeByIds(deleteLearningLogs);
//
//        // 创建失败的列表
//        Map<Long, String> failMap = new HashMap<>();
//        if (!saveBatch) {
//            newLearningLogs.forEach(log -> {
//                if (!learningLogService.save(log)) {
//                    Long clientId = clientUuidMap.get(log.getClientUuid()).getId();
//                    failMap.put(clientId, "新增失败");
//                }
//            });
//        }
//        if (!updateBatch) {
//            updateLearningLogs.forEach(log -> {
//                if (!learningLogService.updateById(log)) {
//                    Long clientId = clientUuidMap.get(log.getClientUuid()).getId();
//                    failMap.put(clientId, "修改失败");
//                }
//            });
//        }
//        if (!deleteBatch) {
//            deleteLearningLogs.forEach(log -> {
//                if (!learningLogService.removeById(log)) {
//                    Long clientId = clientUuidMap.get(log.getClientUuid()).getId();
//                    failMap.put(clientId, "删除失败");
//                }
//            });
//        }
//        SyncInfoReturnParam returnParam = new SyncInfoReturnParam();
//        returnParam.setSyncTime(LocalDateTime.now())
//                .setSyncUserId(UserUtils.get().getId())
//                .setFailMap(failMap);
//        if (failMap.isEmpty())
//            returnParam.setSyncInfo("同步成功");
//        else if (!saveBatch && !updateBatch && !deleteBatch)
//            returnParam.setSyncInfo("同步失败");
//        else
//            returnParam.setSyncInfo("部分同步成功");
//        return R.ok(returnParam);
    }


    /**
     * 通用同步方法
     *
     * @param clientList       客户端传入的对象列表
     * @param serverService    MyBatis-Plus Service
     * @param clientUuidGetter 客户端实体用来唯一标识记录的字段
     * @param serverUuidGetter 服务端实体用来唯一标识记录的字段
     * @param isDeleteGetter   删除标记 getter（返回 1 表示删除，0 表示有效）
     * @param entityClass      实体类 class
     * @param <C>              客户端对象类型
     * @param <S>              服务端对象类型（与数据库对应）
     * @return SyncInfoReturnParam  同步结果（含成功/失败信息）
     */
    public static <C, S> SyncInfoReturnParam syncData(
            List<C> clientList,
            IService<S> serverService,
            SFunction<C, String> clientUuidGetter,
            SFunction<S, String> serverUuidGetter,
            SFunction<C, Long> clientIdGetter,
            SFunction<S, Long> serverIdGetter,
            SFunction<C, Integer> isDeleteGetter,
            Class<S> entityClass
    ) {

        // 客户端 uuid → client
        Map<String, C> clientMap = clientList.stream()
                .collect(Collectors.toMap(clientUuidGetter, c -> c, (a, b) -> a));

        // 查库，获取服务端已有数据
        List<S> serverList = serverService.list(new LambdaQueryWrapper<S>()
                .in(serverUuidGetter, clientMap.keySet()));
        // 服务端 uuid → server
        Map<String, S> serverMap = serverList.stream()
                .collect(Collectors.toMap(serverUuidGetter, s -> s, (a, b) -> a));

        // 三类集合
        List<S> newList = new ArrayList<>();
        List<S> updateList = new ArrayList<>();
        List<S> deleteList = new ArrayList<>();

        for (C client : clientList) {
            String uuid = clientUuidGetter.apply(client);
            S server = serverMap.get(uuid);

            if (server == null) {
                // 不存在 → 新增
                try {
                    S entity = entityClass.newInstance();
                    BeanUtils.copyProperties(client, entity);
                    // 确保主键为空
                    entityClass.getMethod("setId", Long.class).invoke(entity, (Long) null);
                    newList.add(entity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (Objects.equals(isDeleteGetter.apply(client), 1)) {
                // 存在且被删除 → 删除
                deleteList.add(server);
            } else {
                // 存在且未删除 → 修改
                try {
                    S entity = entityClass.newInstance();
                    BeanUtils.copyProperties(client, entity);
                    // 设置主键
                    entityClass.getMethod("setId", Long.class).invoke(entity, serverIdGetter.apply(server));
                    updateList.add(entity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 批量执行
        boolean saveBatch = newList.isEmpty() || serverService.saveBatch(newList);
        boolean updateBatch = updateList.isEmpty() || serverService.updateBatchById(updateList);
        boolean deleteBatch = deleteList.isEmpty() || serverService.removeByIds(deleteList);

        // 失败兜底
        Map<Long, String> failMap = new HashMap<>();
        if (!saveBatch) {
            newList.forEach(log -> {
                if (!serverService.save(log))
                    failMap.put(clientIdGetter.apply(clientMap.get(serverUuidGetter.apply(log))), "新增失败");
            });
        }
        if (!updateBatch) {
            updateList.forEach(log -> {
                if (!serverService.updateById(log))
                    failMap.put(clientIdGetter.apply(clientMap.get(serverUuidGetter.apply(log))), "修改失败");
            });
        }
        if (!deleteBatch) {
            deleteList.forEach(log -> {
                if (!serverService.removeById(log))
                    failMap.put(clientIdGetter.apply(clientMap.get(serverUuidGetter.apply(log))), "删除失败");
            });
        }

        // 6. 构造返回结果
        SyncInfoReturnParam result = new SyncInfoReturnParam();
        result.setSyncTime(LocalDateTime.now())
                .setSyncUserId(UserUtils.get().getId())
                .setFailMap(failMap);
        if (failMap.isEmpty()) {
            result.setSyncInfo("同步成功");
        } else if (!saveBatch && !updateBatch && !deleteBatch) {
            result.setSyncInfo("同步失败");
        } else {
            result.setSyncInfo("部分同步成功");
        }
        return result;
    }
}
