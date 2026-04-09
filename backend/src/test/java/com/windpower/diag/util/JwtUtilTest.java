package com.windpower.diag.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 单元测试
 */
@DisplayName("JWT 工具类测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        // 通过反射注入配置值
        setField(jwtUtil, "secret", "WindPowerDiagSystem2025SecretKeyForJWTTokenGeneration!@#");
        setField(jwtUtil, "expiration", 86400000L);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("generateToken - 生成 Token 不为空")
    void testGenerateToken() {
        String token = jwtUtil.generateToken(1L, "admin");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("generateToken - Token 包含三段（JWT 格式）")
    void testTokenFormat() {
        String token = jwtUtil.generateToken(1L, "admin");
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT 应包含 header.payload.signature 三段");
    }

    @Test
    @DisplayName("getUserId - 从 Token 中提取用户 ID")
    void testGetUserId() {
        String token = jwtUtil.generateToken(42L, "testuser");
        Long userId = jwtUtil.getUserId(token);
        assertEquals(42L, userId);
    }

    @Test
    @DisplayName("getUsername - 从 Token 中提取用户名")
    void testGetUsername() {
        String token = jwtUtil.generateToken(1L, "admin");
        String username = jwtUtil.getUsername(token);
        assertEquals("admin", username);
    }

    @Test
    @DisplayName("validateToken - 有效 Token 校验通过")
    void testValidateTokenValid() {
        String token = jwtUtil.generateToken(1L, "admin");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("validateToken - 无效 Token 校验失败")
    void testValidateTokenInvalid() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
        assertFalse(jwtUtil.validateToken(""));
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    @DisplayName("validateToken - 篡改 Token 校验失败")
    void testValidateTokenTampered() {
        String token = jwtUtil.generateToken(1L, "admin");
        // 篡改 payload 部分
        String tampered = token.substring(0, token.lastIndexOf('.')) + ".tampered";
        assertFalse(jwtUtil.validateToken(tampered));
    }

    @Test
    @DisplayName("isTokenExpired - 新生成的 Token 未过期")
    void testIsTokenExpiredFresh() {
        String token = jwtUtil.generateToken(1L, "admin");
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("isTokenExpired - 过期 Token 检测")
    void testIsTokenExpiredExpired() throws Exception {
        // 设置过期时间为 1ms
        setField(jwtUtil, "expiration", 1L);
        String token = jwtUtil.generateToken(1L, "admin");
        Thread.sleep(10);
        assertTrue(jwtUtil.isTokenExpired(token));
        // 恢复
        setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    @DisplayName("不同用户生成不同 Token")
    void testDifferentUsersGetDifferentTokens() {
        String token1 = jwtUtil.generateToken(1L, "admin");
        String token2 = jwtUtil.generateToken(2L, "user");
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("中文用户名支持")
    void testChineseUsername() {
        String token = jwtUtil.generateToken(1L, "管理员");
        assertEquals("管理员", jwtUtil.getUsername(token));
    }
}
