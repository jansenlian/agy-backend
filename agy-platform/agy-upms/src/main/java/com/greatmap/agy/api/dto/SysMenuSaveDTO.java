package com.greatmap.agy.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * 菜单/按钮新增与修改 DTO (白名单入参校验，双向兼容 menuName 与 title 契约)
 */
@Data
@Schema(description = "菜单与权限点新增/修改参数")
public class SysMenuSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单主键 ID (修改时必传)", example = "1")
    private Long id;

    @Schema(description = "父级菜单 ID (0表示根目录)", example = "0", defaultValue = "0")
    private Long parentId = 0L;

    @Schema(description = "菜单名称", example = "用户管理")
    @Size(min = 1, max = 50, message = "菜单名称长度必须为 1-50 个字符")
    private String menuName;

    @Schema(description = "菜单标题 (别名，兼容 menuName)", example = "用户管理")
    private String title;

    @Schema(description = "前端路由路由访问路径", example = "user")
    private String path;

    @Schema(description = "前端组件物理路径", example = "system/user/index")
    private String component;

    @Schema(description = "权限字符标识 (如 sys:user:add)", example = "sys:user:list")
    private String permission;

    @Schema(description = "菜单类型 (0-目录 1-菜单 2-按钮)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    @Schema(description = "显示排序值 (升序)", example = "1", defaultValue = "0")
    private Integer sortOrder = 0;

    @Schema(description = "Element Plus 图标名称", example = "User")
    private String icon;

    public String getMenuName() {
        if (StringUtils.isNotBlank(menuName)) {
            return menuName;
        }
        return title;
    }

    public String getTitle() {
        if (StringUtils.isNotBlank(title)) {
            return title;
        }
        return menuName;
    }
}
