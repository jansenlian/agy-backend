package com.greatmap.agy.web.controller;

import com.greatmap.agy.api.dto.ChangePasswordDTO;
import com.greatmap.agy.api.dto.PageQueryDTO;
import com.greatmap.agy.api.dto.SysUserCreateDTO;
import com.greatmap.agy.api.dto.SysUserResetPasswordDTO;
import com.greatmap.agy.api.dto.SysUserUpdateDTO;
import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysUserVO;
import com.greatmap.agy.common.annotation.Log;
import com.greatmap.agy.common.aspect.RepeatSubmit;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.common.enums.BusinessType;
import com.greatmap.agy.common.excel.EasyExcelUtils;
import com.greatmap.agy.common.utils.SecurityUtils;
import com.greatmap.agy.service.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "02. 用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('sys:user:list')")
    public Result<PageResultVO<SysUserVO>> getUserPage(@Valid PageQueryDTO dto) {
        PageResultVO<SysUserVO> result = sysUserService.getUserPage(dto);
        return Result.ok(result);
    }

    @Operation(summary = "新增用户 (防重复提交 3 秒)")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @RepeatSubmit(interval = 3000, message = "用户创建中，请勿频繁重复点击")
    public Result<Long> createUser(@Valid @RequestBody SysUserCreateDTO dto) {
        Long userId = sysUserService.createUser(dto);
        return Result.ok(userId);
    }

    @Operation(summary = "修改用户 (使用 SysUserUpdateDTO 白名单防 Mass Assignment)")
    @PutMapping
    @PreAuthorize("@ss.hasPerm('sys:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    public Result<Boolean> updateUser(@Valid @RequestBody SysUserUpdateDTO dto) {
        return Result.ok(sysUserService.updateUser(dto));
    }

    @Operation(summary = "重置用户密码 (管理员操作，使用 POST JSON 传输加密口令)")
    @PostMapping("/{id}/password")
    @PreAuthorize("@ss.hasPerm('sys:user:reset')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE, isSaveRequestData = false)
    public Result<Boolean> resetPassword(@PathVariable Long id, @Valid @RequestBody SysUserResetPasswordDTO dto) {
        return Result.ok(sysUserService.resetPassword(id, dto.getPassword()));
    }

    @Operation(summary = "修改当前登录用户的个人密码")
    @PutMapping("/update-password")
    @PreAuthorize("isAuthenticated()")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE, isSaveRequestData = false)
    public Result<Boolean> updatePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        Long userId = SecurityUtils.getLoginUserId();
        return Result.ok(sysUserService.updatePassword(userId, dto.getOldPassword(), dto.getNewPassword()));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:user:delete')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        return Result.ok(sysUserService.deleteUser(id));
    }

    @Operation(summary = "获取用户已分配的角色 ID 列表")
    @GetMapping("/{id}/roles")
    @PreAuthorize("@ss.hasPerm('sys:user:list')")
    public Result<List<Long>> getUserRoleIds(@PathVariable Long id) {
        return Result.ok(sysUserService.getUserRoleIds(id));
    }

    @Operation(summary = "给用户分配/更新角色")
    @PostMapping("/{id}/roles")
    @PreAuthorize("@ss.hasPerm('sys:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    public Result<Boolean> saveUserRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        return Result.ok(sysUserService.saveUserRoles(id, roleIds));
    }

    @Operation(summary = "导出用户 Excel (EasyExcel 流式防 OOM)")
    @GetMapping("/export")
    @PreAuthorize("@ss.hasPerm('sys:user:export')")
    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    public void exportUsers(HttpServletResponse response) throws IOException {
        PageQueryDTO query = new PageQueryDTO();
        query.setPageSize(1000);
        List<SysUserVO> list = sysUserService.getUserPage(query).getRecords();

        EasyExcelUtils.exportWeb(response, "系统用户数据", "用户列表", SysUserVO.class, list);
    }
}
