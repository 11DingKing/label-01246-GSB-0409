package com.windpower.diag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.windpower.diag.common.BusinessException;
import com.windpower.diag.entity.*;
import com.windpower.diag.mapper.SysEmailVerifyTokenMapper;
import com.windpower.diag.mapper.SysUserMapper;
import com.windpower.diag.mapper.SysUserRoleMapper;
import com.windpower.diag.service.AuthService;
import com.windpower.diag.service.DataScopeService;
import com.windpower.diag.service.EmailService;
import com.windpower.diag.service.LogService;
import com.windpower.diag.service.PermissionService;
import com.windpower.diag.util.JwtUtil;
import com.windpower.diag.util.PasswordEncoder;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Ds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final int MAX_LOGIN_FAIL = 5;
    private static final String DEFAULT_PASSWORD = "123456";

    @Ds
    private SysUserMapper userMapper;
    @Ds
    private SysUserRoleMapper userRoleMapper;
    @Ds
    private SysEmailVerifyTokenMapper emailVerifyTokenMapper;
    @Inject
    private PermissionService permissionService;
    @Inject
    private LogService logService;
    @Inject
    private EmailService emailService;
    @Inject
    private JwtUtil jwtUtil;
    @Inject
    private PasswordEncoder passwordEncoder;
    @Inject("${email-verify.expireMinutes:30}")
    private int verifyExpireMinutes;
    @Inject
    private DataScopeService dataScopeService;

    @Override
    public Map<String, Object> login(String username, String password, String ip, String userAgent) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = userMapper.selectOne(wrapper);

        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(username);
        loginLog.setIp(ip);
        loginLog.setUserAgent(userAgent);
        loginLog.setLoginType(1);
        loginLog.setCreatedAt(LocalDateTime.now());

        if (user == null) {
            loginLog.setStatus(0);
            loginLog.setMessage("用户不存在");
            logService.saveLoginLog(loginLog);
            throw new BusinessException("用户名或密码错误");
        }

        loginLog.setUserId(user.getId());

        // 检查账户状态
        if (user.getStatus() == 0) {
            loginLog.setStatus(0);
            loginLog.setMessage("账户已禁用");
            logService.saveLoginLog(loginLog);
            throw new BusinessException("账户已被禁用，请联系管理员");
        }
        if (user.getStatus() == 2) {
            loginLog.setStatus(0);
            loginLog.setMessage("账户待审核");
            logService.saveLoginLog(loginLog);
            throw new BusinessException("账户正在审核中，请耐心等待");
        }
        if (user.getStatus() == 3) {
            loginLog.setStatus(0);
            loginLog.setMessage("邮箱未验证");
            logService.saveLoginLog(loginLog);
            throw new BusinessException("请先验证邮箱后再登录，如未收到验证邮件请重新发送");
        }
        if (user.getAccountLocked() == 1) {
            loginLog.setStatus(0);
            loginLog.setMessage("账户已锁定");
            logService.saveLoginLog(loginLog);
            throw new BusinessException("账户已被锁定，请联系管理员解锁");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            int failCount = user.getLoginFailCount() + 1;
            SysUser updateUser = new SysUser();
            updateUser.setId(user.getId());
            updateUser.setLoginFailCount(failCount);
            if (failCount >= MAX_LOGIN_FAIL) {
                updateUser.setAccountLocked(1);
                loginLog.setMessage("密码错误，账户已被锁定");
            } else {
                loginLog.setMessage("密码错误，剩余" + (MAX_LOGIN_FAIL - failCount) + "次机会");
            }
            userMapper.updateById(updateUser);
            loginLog.setStatus(0);
            logService.saveLoginLog(loginLog);
            throw new BusinessException(loginLog.getMessage());
        }

        // 登录成功
        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setLoginFailCount(0);
        updateUser.setLastLoginTime(LocalDateTime.now());
        updateUser.setLastLoginIp(ip);
        userMapper.updateById(updateUser);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        loginLog.setStatus(1);
        loginLog.setMessage("登录成功");
        logService.saveLoginLog(loginLog);

        log.info("用户登录成功: {}", username);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        return result;
    }

    @Override
    public void register(String username, String password, String realName, String email) {
        // 空字符串转 null
        if (realName != null && realName.isBlank()) {
            realName = null;
        }
        if (email != null && email.isBlank()) {
            email = null;
        }
        // 邮箱必填（用于邮箱验证）
        if (email == null || email.isEmpty()) {
            throw new BusinessException("邮箱不能为空，注册需要进行邮箱验证");
        }
        // 检查用户名
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 检查邮箱
        LambdaQueryWrapper<SysUser> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(SysUser::getEmail, email);
        if (userMapper.selectCount(emailWrapper) > 0) {
            throw new BusinessException("邮箱已被注册");
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setEmail(email);
        user.setStatus(3); // 待验证邮箱
        user.setEmailVerified(0);
        user.setAccountLocked(0);
        user.setLoginFailCount(0);
        user.setDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            log.error("注册用户失败: username={}, error={}", username, e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                if (e.getMessage().contains("uk_email")) {
                    throw new BusinessException("邮箱已被注册");
                }
                throw new BusinessException("用户名已存在");
            }
            throw e;
        }

        // 默认分配普通查看用户角色
        userRoleMapper.insert(new SysUserRole(user.getId(), 4L));

        // 生成邮箱验证token并发送验证邮件
        String verifyToken = generateAndSaveVerifyToken(user.getId());
        emailService.sendVerifyEmail(email, username, verifyToken);

        log.info("用户注册(待邮箱验证): {}", username);
    }

    @Override
    public void verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("验证Token不能为空");
        }
        // 查找token
        LambdaQueryWrapper<SysEmailVerifyToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEmailVerifyToken::getToken, token);
        SysEmailVerifyToken verifyToken = emailVerifyTokenMapper.selectOne(wrapper);
        if (verifyToken == null) {
            throw new BusinessException("无效的验证链接");
        }
        if (verifyToken.getUsed() == 1) {
            throw new BusinessException("该验证链接已被使用");
        }
        if (verifyToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("验证链接已过期，请重新发送验证邮件");
        }

        // 标记token已使用
        SysEmailVerifyToken updateToken = new SysEmailVerifyToken();
        updateToken.setId(verifyToken.getId());
        updateToken.setUsed(1);
        emailVerifyTokenMapper.updateById(updateToken);

        // 更新用户状态: 待验证邮箱(3) -> 待审核(2)，标记邮箱已验证
        SysUser user = userMapper.selectById(verifyToken.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setEmailVerified(1);
        if (user.getStatus() == 3) {
            updateUser.setStatus(2); // 待审核
        }
        userMapper.updateById(updateUser);

        log.info("邮箱验证成功: userId={}", user.getId());
    }

    @Override
    public void resendVerifyEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("邮箱不能为空");
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getEmail, email);
        SysUser user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("该邮箱未注册");
        }
        if (user.getEmailVerified() != null && user.getEmailVerified() == 1) {
            throw new BusinessException("该邮箱已验证，无需重复验证");
        }
        if (user.getStatus() != 3) {
            throw new BusinessException("当前账户状态不需要邮箱验证");
        }

        String verifyToken = generateAndSaveVerifyToken(user.getId());
        emailService.sendVerifyEmail(email, user.getUsername(), verifyToken);
        log.info("重新发送验证邮件: email={}", email);
    }

    private String generateAndSaveVerifyToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        SysEmailVerifyToken verifyToken = new SysEmailVerifyToken();
        verifyToken.setUserId(userId);
        verifyToken.setToken(token);
        verifyToken.setExpiredAt(LocalDateTime.now().plusMinutes(verifyExpireMinutes));
        verifyToken.setUsed(0);
        verifyToken.setCreatedAt(LocalDateTime.now());
        emailVerifyTokenMapper.insert(verifyToken);
        return token;
    }

    @Override
    public Map<String, Object> getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);

        // 查询角色
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, userId);
        List<Long> roleIds = userRoleMapper.selectList(urWrapper).stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        // 查询权限
        List<SysPermission> permissions = permissionService.getPermissionsByRoleIds(roleIds);
        List<String> permCodes = permissions.stream()
                .map(SysPermission::getPermCode)
                .collect(Collectors.toList());

        // 构建菜单树（自动补全缺失的父级菜单）
        List<SysPermission> menus = permissions.stream()
                .filter(p -> "MENU".equals(p.getPermType()))
                .collect(Collectors.toList());

        // 收集已有菜单的 id
        Set<Long> menuIds = menus.stream().map(SysPermission::getId).collect(Collectors.toSet());

        // 找出需要补全的父级菜单 id
        Set<Long> missingParentIds = new HashSet<>();
        for (SysPermission menu : menus) {
            Long pid = menu.getParentId();
            if (pid != null && pid != 0 && !menuIds.contains(pid)) {
                missingParentIds.add(pid);
            }
        }

        // 从数据库查询缺失的父级菜单并补入
        if (!missingParentIds.isEmpty()) {
            List<SysPermission> parentMenus = permissionService.getPermissionsByIds(new ArrayList<>(missingParentIds));
            menus.addAll(parentMenus);
        }

        List<SysPermission> menuTree = buildMenuTree(menus, 0L);

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("roleIds", roleIds);
        result.put("permissions", permCodes);
        result.put("menus", menuTree);
        result.put("dataScope", dataScopeService.getUserDataScope(userId));
        return result;
    }

    private List<SysPermission> buildMenuTree(List<SysPermission> menus, Long parentId) {
        List<SysPermission> tree = new ArrayList<>();
        for (SysPermission menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                menu.setChildren(buildMenuTree(menus, menu.getId()));
                tree.add(menu);
            }
        }
        return tree;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(updateUser);
        log.info("用户修改密码: userId={}", userId);
    }

    @Override
    public void resetPassword(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        updateUser.setLoginFailCount(0);
        updateUser.setAccountLocked(0);
        userMapper.updateById(updateUser);
        log.info("重置用户密码: userId={}", userId);
    }
}
