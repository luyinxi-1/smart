package com.upc.modular.textbook.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.vo.TeacherReturnVo;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.param.*;
import com.upc.modular.textbook.service.ITextbookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
    @ApiOperation(value = "教材中心按教学讨论查询教材")
    @PostMapping("/getTextbookCenter")
    public R<PageBaseReturnParam<TextbookCenterPageReturnParam>> getTextbookCenter(@RequestBody TextbookCenterPageSearchParam param) {
        Page<TextbookCenterPageReturnParam> page = textbookService.getTextbookCenter(param);
        PageBaseReturnParam<TextbookCenterPageReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }
    /**
     * 教材多条件组合筛选接口
     * @param req 包含所有筛选条件的请求体
     * @return 分页后的教材列表
     */
    @ApiOperation(value = "智能筛选教材(表单版)")
    @PostMapping("/querytextbook")
    public R<PageBaseReturnParam<Textbook>> queryTextbooks(@RequestBody TextbookQueryReq req) {
        Page<Textbook> pageResult = textbookService.queryTextbooksByConditions(req);
        PageBaseReturnParam<Textbook> result = PageBaseReturnParam.ok(pageResult);
        return R.page(result);
    }
    @ApiOperation("教材智能搜索")
    @GetMapping("/smartSearch")
    public R<Page<TextbookIntelligentQueryReturnParam>> smartSearch(
            @ApiParam("搜索关键词") @RequestParam String query,
            @ApiParam("页码") @RequestParam(defaultValue = "1") long current,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") long size) {
        try {
            Page<TextbookIntelligentQueryReturnParam> result = textbookService.smartSearch(query, current, size);
            return R.ok(result);
        } catch (Exception e) {
            return R.fail("搜索失败");
        }
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
/*    @ApiOperation(value = "校验教材状态和版本（含资格审查）")
    @PostMapping("/checkVersion")
    public R<VersionCheckResultDto> checkTextbookVersion(
            @ApiParam(value = "教材的唯一ID", required = true, example = "1001")
            @RequestParam("textbookId") Long textbookId,

            @ApiParam(value = "客户端持有的版本号", required = true, example = "v1.2.0")
            @RequestParam("clientVersion") String clientVersion) {

        // 2. 参数校验 (简单校验)
        if (textbookId == null || clientVersion == null || clientVersion.trim().isEmpty()) {
            return R.fail("参数错误：textbookId 和 clientVersion 不能为空");
        }
        VersionCheckResultDto resultDto = textbookService.checkStatusAndVersion(textbookId, clientVersion);

        return R.ok(resultDto);
    }*/
@ApiOperation(value = "校验教材状态和版本（含资格审查）")
@PostMapping("/checkVersion")
public R<VersionCheckResultDto> checkTextbookVersion(
        @ApiParam(value = "教材的唯一ID", required = true, example = "1001")
        @RequestParam("textbookId") Long textbookId,

        @ApiParam(value = "客户端持有的版本号", required = false, example = "v1.2.0")
        @RequestParam(value = "clientVersion", required = false) String clientVersion) {

    // 只校验 textbookId
    if (textbookId == null) {
        return R.fail("参数错误：textbookId 不能为空");
    }

    VersionCheckResultDto resultDto = textbookService.checkStatusAndVersion(textbookId, clientVersion);
    return R.ok(resultDto);
}


    @ApiOperation(value = "分页查询教材热度排行榜")
    @PostMapping("/getHotnessPage")
    public R<PageBaseReturnParam<TextbookHotnessDto>> getHotnessPage(@RequestBody Page<TextbookHotnessDto> page) {
        Page<TextbookHotnessDto> hotnessPage = textbookService.getTextbookHotnessPage(page);
        PageBaseReturnParam<TextbookHotnessDto> result = PageBaseReturnParam.ok(hotnessPage);
        return R.page(result);
    }

    @ApiOperation(value = "教材内智慧搜索")
    @PostMapping("/smartSearchInTextbook")
    public R<List<TextbookContentSearchResult>> smartSearchInTextbook(
            @RequestParam("textbookId") Long textbookId,
            @RequestParam("query") String query) {
        return R.ok(textbookService.smartSearchInTextbook(textbookId, query));
    }
    
    @ApiOperation(value = "批量更新教材删除状态")
    @PostMapping("/batchUpdateDeleteStatus")
    public R batchUpdateDeleteStatus(@RequestBody BatchUpdateDeleteStatusParam param) {
        textbookService.updateTextbookDeleteStatus(param.getIds(), param.getIsDelete());
        return R.commonReturn(200, "更新成功", "");
    }
}