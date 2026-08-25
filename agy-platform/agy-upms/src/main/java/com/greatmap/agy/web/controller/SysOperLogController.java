package com.greatmap.agy.web.controller;

import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysOperLogVO;
import com.greatmap.agy.common.annotation.Log;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.common.enums.BusinessType;
import com.greatmap.agy.service.service.SysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "05. 操作日志接口")
@RestController
@RequestMapping("/api/logs/oper")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogService operLogService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('sys:log:oper')")
    public Result<PageResultVO<SysOperLogVO>> getOperLogPage(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String operName,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) Integer status) {
        return Result.ok(operLogService.getOperLogPage(pageNo, pageSize, title, operName, businessType, status));
    }

    @Operation(summary = "删除单条操作日志")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:log:oper:delete')")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    public Result<Boolean> deleteOperLog(@PathVariable Long id) {
        return Result.ok(operLogService.removeById(id));
    }

    @Operation(summary = "清空所有操作日志")
    @DeleteMapping("/clean")
    @PreAuthorize("@ss.hasPerm('sys:log:oper:clean')")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    public Result<Boolean> cleanOperLog() {
        return Result.ok(operLogService.remove(null));
    }
}
