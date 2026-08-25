package com.greatmap.agy.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Result 统一 RESTful 响应体契约规范单元测试")
class ResultStructureTest {

    @Test
    @DisplayName("测试 Result.ok() 默认成功结构")
    void testResultOk() {
        Result<Void> res = Result.ok();
        assertNotNull(res);
        assertEquals(200, res.getCode());
        assertEquals("操作成功", res.getMessage());
        assertNull(res.getData());
        assertTrue(res.getTimestamp() > 0);
    }

    @Test
    @DisplayName("测试 Result.success(data) 携带业务数据")
    void testResultSuccessWithData() {
        Map<String, Object> data = Map.of("userId", 1001L, "username", "admin");
        Result<Map<String, Object>> res = Result.success(data);

        assertEquals(200, res.getCode());
        assertEquals("操作成功", res.getMessage());
        assertEquals(data, res.getData());
        assertEquals(1001L, res.getData().get("userId"));
    }

    @Test
    @DisplayName("测试 Result.error() / Result.fail() 默认失败与自定义错误消息")
    void testResultFail() {
        Result<Void> res = Result.fail("用户名或密码错误");
        assertEquals(500, res.getCode());
        assertEquals("用户名或密码错误", res.getMessage());
        assertNull(res.getData());
    }

    @Test
    @DisplayName("测试 Result.error(code, message) 自定义状态码 (如 403 / 500)")
    void testResultFailWithCustomCode() {
        Result<Void> res = Result.error(403, "无权限访问该资源");
        assertEquals(403, res.getCode());
        assertEquals("无权限访问该资源", res.getMessage());
    }

    @Test
    @DisplayName("测试集合泛型数据打包")
    void testResultWithList() {
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_USER");
        Result<List<String>> res = Result.ok(roles);

        assertEquals(200, res.getCode());
        assertEquals(2, res.getData().size());
        assertTrue(res.getData().contains("ROLE_ADMIN"));
    }
}
