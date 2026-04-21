package com.example.getoffer.sql;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SeedKnowledgeDataScriptTest {

    @Test
    void knowledgeSeedScriptShouldResetArticleDataAndContainChineseKnowledgeContent() throws IOException {
        String script = Files.readString(Path.of("sql", "seed-knowledge-data.sql"), StandardCharsets.UTF_8);

        assertAll(
            () -> assertTrue(script.contains("DELETE FROM favorites;")),
            () -> assertTrue(script.contains("DELETE FROM article_tags;")),
            () -> assertTrue(script.contains("DELETE FROM articles;")),
            () -> assertTrue(script.contains("事件循环核心机制梳理")),
            () -> assertTrue(script.contains("Promise 链式调用与错误冒泡")),
            () -> assertTrue(script.contains("泛型的约束与默认类型")),
            () -> assertTrue(script.contains("BFC 的形成条件与应用")),
            () -> assertTrue(script.contains("HTTP 常见状态码与缓存协商")),
            () -> assertTrue(script.contains("Vue 响应式原理速记")),
            () -> assertTrue(script.contains("链表题目的常见解法模板")),
            () -> assertTrue(script.contains("前端性能优化排查思路")),
            () -> assertTrue(script.contains("JavaScript/ES6/TypeScript")),
            () -> assertTrue(script.contains("HTML&CSS")),
            () -> assertTrue(script.contains("计算机网络")),
            () -> assertTrue(script.contains("前端框架")),
            () -> assertTrue(script.contains("数据结构与算法")),
            () -> assertTrue(script.contains("前端纵向领域")),
            () -> assertFalse(script.contains("Vue3 高频面试题整理")),
            () -> assertFalse(script.contains("Spring Security + JWT 实战复盘"))
        );
    }
}
