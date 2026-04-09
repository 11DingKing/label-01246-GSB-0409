package com.windpower.diag.service;

import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.SysUser;
import java.util.List;
import java.util.Map;

public interface AuthService {
    Map<String, Object> login(String username, String password, String ip, String userAgent);
    void register(String username, String password, String realName, String email);
    void verifyEmail(String token);
    void resendVerifyEmail(String email);
    Map<String, Object> getUserInfo(Long userId);
    void changePassword(Long userId, String oldPassword, String newPassword);
    void resetPassword(Long userId);
}
