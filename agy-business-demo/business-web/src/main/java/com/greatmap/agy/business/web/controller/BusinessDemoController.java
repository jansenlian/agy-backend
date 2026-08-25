package com.greatmap.agy.business.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greatmap.agy.business.api.dto.BusinessDemoDTO;
import com.greatmap.agy.business.api.vo.BusinessDemoVO;
import com.greatmap.agy.common.annotation.Log;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.common.enums.BusinessType;
import com.greatmap.agy.business.service.service.BusinessDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 业务演示控制器 (业务领域微服务专属，UPMS 统一托管在 agy-platform)
 */
@Tag(name = "业务演示模块")
@RestController
@RequestMapping("/api/business/demo")
@RequiredArgsConstructor
public class BusinessDemoController {

    private final BusinessDemoService businessDemoService;

    @Operation(summary = "分页查询业务列表")
    @GetMapping("/page")
    @PreAuthorize("@ss.hasPerm('biz:demo:query')")
    public Result<Page<BusinessDemoVO>> getDemoPage(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name) {
        return Result.ok(businessDemoService.getDemoPage(pageNo, pageSize, name));
    }

    @Operation(summary = "创建业务示范数据")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('biz:demo:add')")
    @Log(title = "业务演示", businessType = BusinessType.INSERT)
    public Result<Long> createDemo(@Valid @RequestBody BusinessDemoDTO dto) {
        return Result.ok(businessDemoService.createDemo(dto));
    }
}
