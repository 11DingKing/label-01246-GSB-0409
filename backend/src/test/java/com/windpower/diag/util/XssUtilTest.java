package com.windpower.diag.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XssUtil 单元测试
 */
@DisplayName("XSS 工具类测试")
class XssUtilTest {

    @Test
    @DisplayName("escape - 转义 HTML 特殊字符")
    void testEscape() {
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;",
                XssUtil.escape("<script>alert(1)</script>"));
        assertEquals("&lt;img onerror=&quot;alert(1)&quot;&gt;",
                XssUtil.escape("<img onerror=\"alert(1)\">"));
        assertEquals("hello &amp; world",
                XssUtil.escape("hello & world"));
        assertEquals("it&#x27;s a test",
                XssUtil.escape("it's a test"));
    }

    @Test
    @DisplayName("escape - null 和空字符串")
    void testEscapeNullAndEmpty() {
        assertNull(XssUtil.escape(null));
        assertEquals("", XssUtil.escape(""));
    }

    @Test
    @DisplayName("escape - 正常文本不变")
    void testEscapeNormalText() {
        assertEquals("hello world", XssUtil.escape("hello world"));
        assertEquals("用户名123", XssUtil.escape("用户名123"));
        assertEquals("test@email.com", XssUtil.escape("test@email.com"));
    }

    @Test
    @DisplayName("stripTags - 清除 HTML 标签")
    void testStripTags() {
        assertEquals("alert(1)", XssUtil.stripTags("<script>alert(1)</script>"));
        assertEquals("hello", XssUtil.stripTags("<b>hello</b>"));
        assertEquals("", XssUtil.stripTags("<img src=x onerror=alert(1)>"));
    }

    @Test
    @DisplayName("stripTags - null 和空字符串")
    void testStripTagsNullAndEmpty() {
        assertNull(XssUtil.stripTags(null));
        assertEquals("", XssUtil.stripTags(""));
    }

    @Test
    @DisplayName("containsXss - 检测 script 标签")
    void testContainsXssScript() {
        assertTrue(XssUtil.containsXss("<script>alert(1)</script>"));
        assertTrue(XssUtil.containsXss("<SCRIPT>alert(1)</SCRIPT>"));
        assertTrue(XssUtil.containsXss("<Script src='evil.js'>"));
    }

    @Test
    @DisplayName("containsXss - 检测事件处理器")
    void testContainsXssEventHandlers() {
        assertTrue(XssUtil.containsXss("<img onerror=alert(1)>"));
        assertTrue(XssUtil.containsXss("<body onload=alert(1)>"));
        assertTrue(XssUtil.containsXss("<a onclick=alert(1)>"));
        assertTrue(XssUtil.containsXss("<div onmouseover=alert(1)>"));
        assertTrue(XssUtil.containsXss("<input onfocus=alert(1)>"));
    }

    @Test
    @DisplayName("containsXss - 检测 javascript: 协议")
    void testContainsXssJavascriptProtocol() {
        assertTrue(XssUtil.containsXss("javascript:alert(1)"));
        assertTrue(XssUtil.containsXss("JAVASCRIPT:void(0)"));
    }

    @Test
    @DisplayName("containsXss - 检测 eval 和 expression")
    void testContainsXssEvalExpression() {
        assertTrue(XssUtil.containsXss("eval('code')"));
        assertTrue(XssUtil.containsXss("expression(alert(1))"));
        assertTrue(XssUtil.containsXss("vbscript:msgbox"));
    }

    @Test
    @DisplayName("containsXss - 正常文本不误报")
    void testContainsXssNormalText() {
        assertFalse(XssUtil.containsXss("hello world"));
        assertFalse(XssUtil.containsXss("用户名"));
        assertFalse(XssUtil.containsXss("test@email.com"));
        assertFalse(XssUtil.containsXss("price > 100"));
        assertFalse(XssUtil.containsXss("a < b"));
        assertFalse(XssUtil.containsXss(""));
        assertFalse(XssUtil.containsXss(null));
    }
}
