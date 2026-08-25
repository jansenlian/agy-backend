package com.greatmap.agy.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单与权限标识树形 VO
 */
@Data
@Schema(description = "树形菜单与权限节点视图信息")
public class SysMenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单主键 ID", example = "1")
    private Long id;

    @Schema(description = "父级菜单 ID", example = "0")
    private Long parentId;

    @Schema(description = "菜单显示名称", example = "系统管理")
    private String menuName;

    @Schema(description = "前端访问路径", example = "system")
    private String path;

    @Schema(description = "前端组件物理路径", example = "system/user/index")
    private String component;

    @Schema(description = "权限标识字符 (如 sys:user:add)", example = "sys:user:list")
    private String permission;

    @Schema(description = "Element Plus 图标", example = "Setting")
    private String icon;

    @Schema(description = "显示排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "菜单类型 (0-目录 1-菜单 2-按钮)", example = "0")
    private Integer menuType;

    /**
     * 子菜单树节点
     */
    @Schema(description = "子菜单与下级权限节点列表")
    private List<SysMenuVO> children = new ArrayList<>();
}
