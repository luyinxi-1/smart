package com.upc.modular.textbook.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.upc.modular.textbook.entity.TextbookTemplate;
import com.upc.modular.textbook.service.ITextbookTemplateService;
import com.upc.common.responseparam.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/textbookTemplate")
public class TextbookTemplateController {

    @Autowired
    private ITextbookTemplateService textbookTemplateService;

    /**
     * 新增模板
     */
    @PostMapping("/add")
    public R addTemplate(@RequestBody TextbookTemplate template) {
        textbookTemplateService.addTemplate(template);
        return R.ok();
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/delete")
    public R deleteById(@RequestParam Long id) {
        textbookTemplateService.deleteById(id);
        return R.ok();
    }

    /**
     * 分页查询模板
     */
    @GetMapping("/page")
    public R<IPage<TextbookTemplate>> pageByTextbookIdAndStatus(
            @RequestParam Long textbookId,
            @RequestParam Long status,
            @RequestParam(defaultValue = "1") Long pageNo,
            @RequestParam(defaultValue = "10") Long pageSize) {
        IPage<TextbookTemplate> page = textbookTemplateService.pageByTextbookIdAndStatus(textbookId, status, pageNo, pageSize);
        return R.ok(page);
    }

    /**
     * 设置启用模板
     */
    @PostMapping("/setActive")
    public R setActiveTemplate(@RequestBody setActiveTemplateRequest request) {
        textbookTemplateService.setActiveTemplate(request.getTextbookId(), request.getTemplateId());
        return R.ok();
    }

    /**
     * 查询当前启用模板
     */
    @GetMapping("/active")
    public R<TextbookTemplate> getActiveTemplate(@RequestParam Long textbookId) {
        TextbookTemplate template = textbookTemplateService.getActiveTemplate(textbookId);
        return R.ok(template);
    }

    /**
     * 设置启用模板请求参数类
     */
    static class setActiveTemplateRequest {
        private Long textbookId;
        private Long templateId;

        public Long getTextbookId() {
            return textbookId;
        }

        public void setTextbookId(Long textbookId) {
            this.textbookId = textbookId;
        }

        public Long getTemplateId() {
            return templateId;
        }

        public void setTemplateId(Long templateId) {
            this.templateId = templateId;
        }
    }
}