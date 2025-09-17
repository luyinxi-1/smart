package com.upc.modular.textbook.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.vo.TeacherReturnVo;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.param.TextbookPageReturnParam;
import com.upc.modular.textbook.param.TextbookPageSearchParam;
import com.upc.modular.textbook.param.UserFavoritesPageSearch;
import com.upc.modular.textbook.param.UserFavoritesVO;
import com.upc.modular.textbook.service.ITextbookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-07-08
 */
@RestController
@RequestMapping("/textbook")
@Api(tags = "教材管理")
public class TextbookController {
    @Autowired
    private ITextbookService textbookService;

    @ApiOperation(value = "新增教材")
    @PostMapping("/insert")
    public R insert(@RequestBody Textbook textbook) {
        textbookService.insert(textbook);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "删除教材")
    @PostMapping("/batchDelete")
    public R batchDelete(@RequestBody IdParam idParam) {
        textbookService.deleteDictItemByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "修改教材")
    @PostMapping("/update")
    public R update(@RequestBody Textbook textbook) {
        textbookService.updateTextbook(textbook);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "分页查询教材")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<TextbookPageReturnParam>> getPage(@RequestBody TextbookPageSearchParam param) {
        Page<TextbookPageReturnParam> page = textbookService.getPage(param);
        PageBaseReturnParam<TextbookPageReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "查询最新的教材")
    @PostMapping("/getNewTextbook")
    public R<List<Textbook>> getNewTextbook(@RequestParam("getNumber") int getNumber) {
        return R.ok(textbookService.getNewTextbook(getNumber));
    }

    @ApiOperation(value = "查询单本教材详情")
    @PostMapping("/getOneTextbookDetails")
    public R<TextbookPageReturnParam> getOneTextbookDetails(@RequestParam("textbookId") Long textbookId) {
        return R.ok(textbookService.getOneTextbookDetails(textbookId));
    }

    @ApiOperation(value = "分页查询教材中心")
    @PostMapping("/getpageTextbookCenter")
    public R<PageBaseReturnParam<Textbook>> getpageTextbookCenter(@RequestBody UserFavoritesPageSearch param) {
        Page<Textbook> page = textbookService.getpageTextbookCenter(param);
        PageBaseReturnParam<Textbook> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "下载教材信息")
    @GetMapping("/downloadTextbookInfo")
    public R<Textbook> downloadTextbookInfo(@RequestParam("textbookId") Long textbookId) {
        return R.ok(textbookService.downloadTextbookInfo(textbookId));
    }

/*    @ApiOperation(value = "测试post")
    @PostMapping("/testPost")
    public R<String> testPost(@RequestBody Textbook textbook, @RequestParam("textbookId") String textbookId) {
        System.out.println("textbookId:" + textbookId);
        System.out.println("textbook:" + textbook);
        return R.ok("测试成功");
    }*/
@ApiOperation(value = "测试post并校验版本（含资格审查）")
@PostMapping("/testPost")
public R<String> testPost(@RequestBody Textbook textbook,
                          @RequestParam("textbookId") Long textbookId,
                          @RequestParam("clientVersion") String clientVersion) {

    System.out.println("服务器收到版本校验请求 - 教材ID: " + textbookId);

    // 1. 从数据库获取教材的完整信息
    Textbook serverTextbook = textbookService.getById(textbookId);

    // 2. 检查教材是否存在
    if (serverTextbook == null) {
        return R.fail("服务器未找到ID为 " + textbookId + " 的教材");
    }

    // ==================== 新增的核心业务资格审查逻辑 ====================
    Integer releaseStatus = serverTextbook.getReleaseStatus();
    Integer reviewStatus = serverTextbook.getReviewStatus(); // 假设有 getReviewStatus() 方法

    System.out.println("资格审查 - 发布状态: " + releaseStatus + ", 审查状态: " + reviewStatus);

    JSONObject responseJson = new JSONObject();
    responseJson.put("textbookId", textbookId);

    // 3. 判断是否满足前置条件
    //    我们假设状态为 1 代表 "已发布" 和 "已审查"
    boolean isAvailable = (releaseStatus != null && releaseStatus.equals(1)) &&
            (reviewStatus != null && reviewStatus.equals(1));

    if (!isAvailable) {
        // **情况A：资格审查不通过**
        // 教材未发布或未审查，直接返回一个明确的“不可用”状态，不进行版本比较
        System.out.println("资格审查不通过，教材当前不可用。");
        responseJson.put("status", "UNAVAILABLE"); // 使用一个清晰的状态名
        responseJson.put("message", "该教材当前未发布或未通过审查，无法进行版本比较。");
        return R.ok(responseJson.toJSONString());
    }
    // =================================================================

    // 4. **只有在资格审查通过后，才执行原有的版本比较逻辑**
    System.out.println("资格审查通过，开始进行版本比较...");
    String serverVersion = serverTextbook.getTextbookVersion();

    if (serverVersion.equals(clientVersion)) {
        // **情况B：资格审查通过，且版本一致**
        System.out.println("版本号一致。");
        responseJson.put("status", "MATCH");
        responseJson.put("message", "版本一致，无需更新。");
    } else {
        // **情况C：资格审查通过，但版本不一致**
        System.out.println("版本号不一致！服务器版本: " + serverVersion + ", 客户端版本: " + clientVersion);
        responseJson.put("status", "MISMATCH");
        responseJson.put("serverVersion", serverVersion);
        responseJson.put("message", "版本不一致，建议更新。");
    }

    return R.ok(responseJson.toJSONString());
}
}
