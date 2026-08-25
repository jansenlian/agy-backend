package com.greatmap.agy.web.controller;

import com.greatmap.agy.api.vo.SysMenuVO;
import com.greatmap.agy.common.annotation.Log;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.common.enums.BusinessType;
import com.greatmap.agy.service.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "03. 菜单管理接口")
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @Operation(summary = "获取当前登录用户菜单列表树 (用于侧边栏渲染)")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysMenuVO>> getMenuList() {
        Long userId = com.greatmap.agy.common.utils.SecurityUtils.getLoginUserId();
        List<SysMenuVO> tree = sysMenuService.getUserMenuTree(userId);
        return Result.ok(tree);
    }

    @Operation(summary = "获取全量菜单与按钮树形节点 (用于菜单管理与角色授权树)")
    @GetMapping("/tree")
    @PreAuthorize("@ss.hasPerm('sys:menu:list')")
    public Result<List<SysMenuVO>> getAllMenuTree() {
        return Result.ok(sysMenuService.getAllMenuTree());
    }

    @Operation(summary = "新增菜单/按钮")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:menu:add')")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    public Result<Boolean> addMenu(@jakarta.validation.Valid @RequestBody com.greatmap.agy.api.dto.SysMenuSaveDTO dto) {
        return Result.ok(sysMenuService.saveMenu(dto));
    }

    @Operation(summary = "修改菜单/按钮")
    @PutMapping
    @PreAuthorize("@ss.hasPerm('sys:menu:edit')")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    public Result<Boolean> updateMenu(@jakarta.validation.Valid @RequestBody com.greatmap.agy.api.dto.SysMenuSaveDTO dto) {
        return Result.ok(sysMenuService.updateMenu(dto));
    }

    @Operation(summary = "删除菜单/按钮")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:menu:delete')")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    public Result<Boolean> deleteMenu(@PathVariable Long id) {
        return Result.ok(sysMenuService.deleteMenu(id));
    }
}
