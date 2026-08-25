package com.greatmap.agy.web.controller;

import com.greatmap.agy.api.dto.SysRoleSaveDTO;
import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysRoleVO;
import com.greatmap.agy.common.annotation.Log;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.common.enums.BusinessType;
import com.greatmap.agy.service.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色与权限管理接口")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @Operation(summary = "分页获取角色列表")
    @GetMapping("/page")
    @PreAuthorize("@ss.hasPerm('sys:role:list')")
    public Result<PageResultVO<SysRoleVO>> getRolePage(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode) {
        return Result.ok(roleService.getRolePage(pageNo, pageSize, roleName, roleCode));
    }

    @Operation(summary = "获取所有角色列表")
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPerm('sys:role:list')")
    public Result<List<SysRoleVO>> getRoleList() {
        return Result.ok(roleService.getAllRoles());
    }

    @Operation(summary = "获取角色的菜单与权限标识 ID 集合")
    @GetMapping("/{id}/menu-ids")
    @PreAuthorize("@ss.hasPerm('sys:role:list')")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long id) {
        return Result.ok(roleService.getRoleMenuIds(id));
    }

    @Operation(summary = "保存角色的菜单与权限点授权绑定")
    @PostMapping("/{id}/authorize")
    @PreAuthorize("@ss.hasPerm('sys:role:authorize')")
    @Log(title = "角色权限管理", businessType = BusinessType.GRANT)
    public Result<Boolean> saveRoleMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        return Result.ok(roleService.saveRoleMenus(id, menuIds));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:role:add')")
    @Log(title = "角色权限管理", businessType = BusinessType.INSERT)
    public Result<Boolean> addRole(@jakarta.validation.Valid @RequestBody com.greatmap.agy.api.dto.SysRoleSaveDTO dto) {
        return Result.ok(roleService.saveRole(dto));
    }

    @Operation(summary = "修改角色")
    @PutMapping
    @PreAuthorize("@ss.hasPerm('sys:role:edit')")
    @Log(title = "角色权限管理", businessType = BusinessType.UPDATE)
    public Result<Boolean> updateRole(@jakarta.validation.Valid @RequestBody com.greatmap.agy.api.dto.SysRoleSaveDTO dto) {
        return Result.ok(roleService.updateRole(dto));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:role:delete')")
    @Log(title = "角色权限管理", businessType = BusinessType.DELETE)
    public Result<Boolean> deleteRole(@PathVariable Long id) {
        return Result.ok(roleService.deleteRole(id));
    }
}
