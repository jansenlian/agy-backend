package com.greatmap.agy.web.controller;

import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysLoginLogVO;
import com.greatmap.agy.common.annotation.Log;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.common.enums.BusinessType;
import com.greatmap.agy.service.service.SysLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "04. 登录日志")
@RestController
@RequestMapping("/api/logs/login")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLoginLogService sysLoginLogService;

    @Operation(summary = "条件分页查询登录日志列表")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('sys:log:list')")
    public Result<PageResultVO<SysLoginLogVO>> getLogPage(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) Integer status) {
        PageResultVO<SysLoginLogVO> result = sysLoginLogService.getLogPage(pageNo, pageSize, username, ipAddress, status);
        return Result.success(result);
    }

    @Operation(summary = "删除单条登录日志")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:log:list')")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    public Result<Boolean> deleteLog(@PathVariable Long id) {
        return Result.ok(sysLoginLogService.removeById(id));
    }
}
