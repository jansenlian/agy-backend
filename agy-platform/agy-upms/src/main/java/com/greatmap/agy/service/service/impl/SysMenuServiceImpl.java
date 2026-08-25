package com.greatmap.agy.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greatmap.agy.api.dto.SysMenuSaveDTO;
import com.greatmap.agy.api.vo.SysMenuVO;
import com.greatmap.agy.common.exception.BusinessException;
import com.greatmap.agy.service.entity.SysMenu;
import com.greatmap.agy.service.mapper.SysMenuMapper;
import com.greatmap.agy.service.mapper.SysRoleMapper;
import com.greatmap.agy.service.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMapper sysRoleMapper;

    private boolean isSuperAdmin(Long userId) {
        if (userId == null) return false;
        if (userId == 1L) return true;
        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(userId);
        return roleCodes != null && roleCodes.contains("ROLE_ADMIN");
    }

    @Override
    public List<SysMenuVO> getUserMenuTree(Long userId) {
        List<SysMenu> menus;
        if (isSuperAdmin(userId)) {
            menus = this.list(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getDelFlag, 0)
                    .orderByAsc(SysMenu::getSortOrder));
        } else {
            menus = sysMenuMapper.selectMenuListByUserId(userId);
        }

        List<SysMenuVO> voList = menus.stream()
                .map(m -> BeanUtil.copyProperties(m, SysMenuVO.class))
                .collect(Collectors.toList());

        return buildTree(voList);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        if (isSuperAdmin(userId)) {
            List<String> perms = new ArrayList<>();
            perms.add("*:*:*");
            List<SysMenu> allMenus = this.list(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getDelFlag, 0)
                    .isNotNull(SysMenu::getPermission));
            allMenus.forEach(m -> {
                if (m.getPermission() != null && !m.getPermission().isBlank()) {
                    perms.add(m.getPermission());
                }
            });
            return perms;
        }
        return sysMenuMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public List<SysMenuVO> getAllMenuTree() {
        List<SysMenu> menus = this.list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getDelFlag, 0)
                .orderByAsc(SysMenu::getSortOrder));

        if (menus.isEmpty()) {
            return new ArrayList<>();
        }

        List<SysMenuVO> voList = menus.stream()
                .map(m -> BeanUtil.copyProperties(m, SysMenuVO.class))
                .collect(Collectors.toList());

        return buildTree(voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMenu(SysMenuSaveDTO dto) {
        if (cn.hutool.core.util.StrUtil.isBlank(dto.getMenuName())) {
            throw new BusinessException("菜单名称不能为空");
        }
        SysMenu menu = BeanUtil.copyProperties(dto, SysMenu.class);
        menu.setMenuName(dto.getMenuName());
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        return this.save(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenu(SysMenuSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("更新菜单 ID 不能为空");
        }
        if (dto.getParentId() != null && dto.getParentId().equals(dto.getId())) {
            throw new BusinessException("上级菜单不能选择自身");
        }
        if (cn.hutool.core.util.StrUtil.isBlank(dto.getMenuName())) {
            throw new BusinessException("菜单名称不能为空");
        }
        SysMenu oldMenu = this.getById(dto.getId());
        if (oldMenu == null) {
            throw new BusinessException("菜单不存在");
        }
        BeanUtil.copyProperties(dto, oldMenu);
        oldMenu.setMenuName(dto.getMenuName());
        return this.updateById(oldMenu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMenu(Long menuId) {
        if (menuId == null) {
            throw new BusinessException("菜单 ID 不能为空");
        }
        Long count = this.count(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, menuId)
                .eq(SysMenu::getDelFlag, 0));
        if (count > 0) {
            throw new BusinessException("该菜单存在下级子菜单或按钮，请先删除子节点");
        }
        return this.removeById(menuId);
    }

    private List<SysMenuVO> buildTree(List<SysMenuVO> nodes) {
        Map<Long, List<SysMenuVO>> childrenMap = nodes.stream()
                .filter(node -> node.getParentId() != null)
                .collect(Collectors.groupingBy(SysMenuVO::getParentId));

        nodes.forEach(node -> node.setChildren(childrenMap.getOrDefault(node.getId(), new ArrayList<>())));

        return nodes.stream()
                .filter(node -> node.getParentId() == null || node.getParentId() == 0L)
                .collect(Collectors.toList());
    }
}
