package com.inventory.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 统一返回体单元测试
 *
 * @author inventory-system
 * @since 2026-08-06
 */
@DisplayName("统一返回体测试 (Result)")
class ResultTest {

    @Test
    @DisplayName("ok() 返回 code=200 和默认消息")
    void okReturnsCode200AndDefaultMessage() {
        Result<String> result = Result.ok("data");

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("data", result.getData());
    }

    @Test
    @DisplayName("ok(message, data) 返回自定义消息")
    void okWithMessageReturnsCustomMessage() {
        Result<Integer> result = Result.ok("创建成功", 42);

        assertEquals(200, result.getCode());
        assertEquals("创建成功", result.getMessage());
        assertEquals(42, result.getData());
    }

    @Test
    @DisplayName("fail() 返回指定 code+message 且 data 为 null")
    void failReturnsSpecifiedCodeAndMessage() {
        Result<Void> result = Result.fail(400, "库存不足");

        assertEquals(400, result.getCode());
        assertEquals("库存不足", result.getMessage());
        assertNull(result.getData());
    }
}
