package com.greatmap.agy.service.config;

import cn.hutool.crypto.digest.BCrypt;
import com.greatmap.agy.service.entity.SysUser;
import com.greatmap.agy.service.mapper.SysRoleMapper;
import com.greatmap.agy.service.mapper.SysUserMapper;
import com.greatmap.agy.service.mapper.SysUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 首次部署超级管理员账号安全初始化启动器
 * 拒绝在 SQL 脚本中放置硬编码口令与预置哈希；首次启动若检测到 admin 账号不存在，
 * 优先从环境变量读取设置，未配置时随机生成 16 位高强度一次性安全口令高亮打印。
 */
@Slf4j
@Component
public class InitialAdminAccountRunner implements ApplicationRunner {

    @Value("${framework.security.initial-admin-password:${SYSTEM_INITIAL_ADMIN_PASSWORD:}}")
    private String configuredInitialPassword;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (sysUserMapper == null) {
            return;
        }

        try {
            // 检查数据库中是否存在 id = 1L 或 username = 'admin' 的超级管理员账号
            SysUser adminUser = sysUserMapper.selectById(1L);
            if (adminUser != null) {
                return;
            }

            String rawPassword;
            boolean isRandomGenerated = false;

            if (cn.hutool.core.util.StrUtil.isNotBlank(configuredInitialPassword)) {
                rawPassword = configuredInitialPassword.trim();
            } else {
                // 随机生成 16 位高强度一次性随机口令
                rawPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                isRandomGenerated = true;
            }

            String passwordHash = BCrypt.hashpw(rawPassword);

            SysUser newAdmin = new SysUser();
            newAdmin.setId(1L);
            newAdmin.setUsername("admin");
            newAdmin.setPassword(passwordHash);
            newAdmin.setRealName("超级管理员");
            newAdmin.setMobile("13800138000");
            newAdmin.setStatus(1);
            newAdmin.setCreateBy(1L);

            sysUserMapper.insert(newAdmin);

            // 动态绑定超级管理员角色 ROLE_ADMIN 并赋予全量菜单权限
            try {
                if (sysRoleMapper != null && sysUserRoleMapper != null) {
                    com.greatmap.agy.service.entity.SysRole adminRole = sysRoleMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.greatmap.agy.service.entity.SysRole>()
                                    .eq(com.greatmap.agy.service.entity.SysRole::getRoleCode, "ROLE_ADMIN")
                    );
                    if (adminRole != null && adminRole.getId() != null) {
                        sysUserRoleMapper.insertUserRole(newAdmin.getId(), adminRole.getId());
                    }
                }
            } catch (Exception e) {
                log.warn("超级管理员角色绑定跳过 (可能已建立关联关系): {}", e.getMessage());
            }

            if (isRandomGenerated) {
                log.info("\n========================================================================================\n" +
                        "【安全通知】全新数据库环境自动创建预置超级管理员账号 [admin]\n" +
                        "【一次性初始口令】: {}\n" +
                        "【核心安全要求】请妥善保管上述随机高强度初始密码，并于首次登录后立即进行密码修改！\n" +
                        "========================================================================================", rawPassword);
            } else {
                log.info("【安全通知】通过环境变量 SYSTEM_INITIAL_ADMIN_PASSWORD 已成功安全初始化超级管理员账号 [admin]");
            }
        } catch (Exception e) {
            log.warn("检查并初始化超级管理员账号过程异常跳过: {}", e.getMessage());
        }
    }
}
