package com.greatmap.agy.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greatmap.agy.api.dto.LoginRequestDTO;
import com.greatmap.agy.api.dto.PageQueryDTO;
import com.greatmap.agy.api.dto.SysUserCreateDTO;
import com.greatmap.agy.api.dto.SysUserUpdateDTO;
import com.greatmap.agy.api.vo.LoginResponseVO;
import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysUserVO;
import com.greatmap.agy.service.entity.SysUser;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    /**
     * 账号密码登录 (支持 RSA 解密与双令牌签发)
     */
    LoginResponseVO login(LoginRequestDTO dto);

    /**
     * 7 天 Refresh Token 无感静默刷新 Access Token
     */
    LoginResponseVO refreshToken(String refreshToken);

    /**
     * 获取前端登录 RSA 加密公钥
     */
    String getPublicKey();

    /**
     * 分页查询用户列表
     */
    PageResultVO<SysUserVO> getUserPage(PageQueryDTO dto);

    /**
     * 创建用户
     */
    Long createUser(SysUserCreateDTO dto);

    /**
     * 修改用户 (使用 SysUserUpdateDTO 白名单防 Mass Assignment)
     */
    boolean updateUser(SysUserUpdateDTO dto);

    /**
     * 重置用户密码 (管理员操作，传入密文或明文，内部统一 Fail-Closed 校验与解密)
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 修改个人密码 (当前登录用户操作)
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 获取用户已绑定的角色 ID 列表
     */
    List<Long> getUserRoleIds(Long userId);

    /**
     * 保存用户分配的角色列表
     */
    boolean saveUserRoles(Long userId, List<Long> roleIds);

    /**
     * 安全删除用户 (内置超级管理员禁止删除，并级联清理关联角色数据)
     */
    boolean deleteUser(Long userId);
}
