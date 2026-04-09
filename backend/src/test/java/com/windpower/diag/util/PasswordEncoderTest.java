package com.windpower.diag.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PasswordEncoder 单元测试
 */
@DisplayName("密码编码器测试")
class PasswordEncoderTest {

    private final PasswordEncoder encoder = new PasswordEncoder();

    @Test
    @DisplayName("encode - 密码加密后不等于原文")
    void testEncodeNotPlaintext() {
        String raw = "admin123";
        String encoded = encoder.encode(raw);
        assertNotNull(encoded);
        assertNotEquals(raw, encoded);
        assertTrue(encoded.startsWith("$2"));  // BCrypt 格式
    }

    @Test
    @DisplayName("encode - 相同密码每次加密结果不同（随机盐）")
    void testEncodeDifferentEachTime() {
        String raw = "password123";
        String encoded1 = encoder.encode(raw);
        String encoded2 = encoder.encode(raw);
        assertNotEquals(encoded1, encoded2);
    }

    @Test
    @DisplayName("matches - 正确密码匹配成功")
    void testMatchesCorrectPassword() {
        String raw = "admin123";
        String encoded = encoder.encode(raw);
        assertTrue(encoder.matches(raw, encoded));
    }

    @Test
    @DisplayName("matches - 错误密码匹配失败")
    void testMatchesWrongPassword() {
        String raw = "admin123";
        String encoded = encoder.encode(raw);
        assertFalse(encoder.matches("wrongpassword", encoded));
    }

    @Test
    @DisplayName("matches - 空密码处理")
    void testMatchesEmptyPassword() {
        String encoded = encoder.encode("test");
        assertFalse(encoder.matches("", encoded));
    }

    @Test
    @DisplayName("encode - 中文密码支持")
    void testEncodeChinesePassword() {
        String raw = "密码测试123";
        String encoded = encoder.encode(raw);
        assertNotNull(encoded);
        assertTrue(encoder.matches(raw, encoded));
    }

    @Test
    @DisplayName("encode - 特殊字符密码支持")
    void testEncodeSpecialCharacters() {
        String raw = "P@$$w0rd!#%^&*()";
        String encoded = encoder.encode(raw);
        assertNotNull(encoded);
        assertTrue(encoder.matches(raw, encoded));
    }

    @Test
    @DisplayName("encode - 长密码支持")
    void testEncodeLongPassword() {
        String raw = "a".repeat(100);
        String encoded = encoder.encode(raw);
        assertNotNull(encoded);
        assertTrue(encoder.matches(raw, encoded));
    }
}
