package com.windpower.diag.controller;

import com.windpower.diag.aop.OperationLog;
import com.windpower.diag.common.Result;
import com.windpower.diag.service.AuthService;
import com.windpower.diag.util.IpUtil;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.validation.annotation.NotBlank;

import java.util.Map;

@Controller
@Mapping("/api/auth")
public class AuthController {

    @Inject
    private AuthService authService;

    @Post
    @Mapping("/login")
    public Result<Map<String, Object>> login(@Body Map<String, String> params, Context ctx) {
        String username = params.get("username");
        String password = params.get("password");
        if (username == null || username.isBlank()) {
            return Result.fail(400, "用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            return Result.fail(400, "密码不能为空");
        }
        String ip = IpUtil.getClientIp(ctx);
        String userAgent = ctx.header("User-Agent");
        Map<String, Object> data = authService.login(username, password, ip, userAgent);
        return Result.ok("登录成功", data);
    }

    @Post
    @Mapping("/register")
    public Result<Void> register(@Body Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String realName = params.get("realName");
        String email = params.get("email");
        if (username == null || username.isBlank()) {
            return Result.fail(400, "用户名不能为空");
        }
        if (password == null || password.isBlank() || password.length() < 6) {
            return Result.fail(400, "密码不能为空且长度不少于6位");
        }
        if (email == null || email.isBlank()) {
            return Result.fail(400, "邮箱不能为空，注册需要进行邮箱验证");
        }
        if (!email.matches("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")) {
            return Result.fail(400, "邮箱格式不正确");
        }
        authService.register(username, password, realName, email);
        return Result.ok("注册成功，验证邮件已发送至您的邮箱，请查收并完成验证", null);
    }

    @Get
    @Mapping("/verify-email")
    public Result<String> verifyEmail(@Param String token) {
        authService.verifyEmail(token);
        return Result.ok("邮箱验证成功！您的账户已进入审核流程，请等待管理员审核。", null);
    }

    @Post
    @Mapping("/resend-verify-email")
    public Result<Void> resendVerifyEmail(@Body Map<String, String> params) {
        String email = params.get("email");
        if (email == null || email.isBlank()) {
            return Result.fail(400, "邮箱不能为空");
        }
        authService.resendVerifyEmail(email);
        return Result.ok("验证邮件已重新发送，请查收", null);
    }

    @Post
    @Mapping("/logout")
    public Result<Void> logout() {
        // JWT 无状态，客户端删除 token 即可
        return Result.ok("登出成功", null);
    }

    @Get
    @Mapping("/info")
    public Result<Map<String, Object>> getUserInfo(Context ctx) {
        Long userId = (Long) ctx.attr("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        Map<String, Object> data = authService.getUserInfo(userId);
        return Result.ok(data);
    }

    @Put
    @Mapping("/password")
    @OperationLog(module = "认证管理", operation = "修改密码")
    public Result<Void> changePassword(@Body Map<String, String> params, Context ctx) {
        Long userId = (Long) ctx.attr("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || oldPassword.isBlank()) {
            return Result.fail(400, "原密码不能为空");
        }
        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 6) {
            return Result.fail(400, "新密码不能为空且长度不少于6位");
        }
        authService.changePassword(userId, oldPassword, newPassword);
        return Result.ok("密码修改成功", null);
    }

    @Post
    @Mapping("/reset-password")
    @OperationLog(module = "认证管理", operation = "重置密码")
    public Result<Void> resetPassword(@Body Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        authService.resetPassword(userId);
        return Result.ok("密码已重置为默认密码: 123456", null);
    }
}
