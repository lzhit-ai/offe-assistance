package com.example.getoffer.sql;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SeedDemoDataScriptTest {

    @Test
    void seedScriptUsesChineseTitlesCategoriesAndTagsForDemoArticles() throws IOException {
        String script = Files.readString(Path.of("sql", "seed-demo-data.sql"), StandardCharsets.UTF_8);

        assertTrue(script.contains("Vue3 高频面试题整理"));
        assertTrue(script.contains("Spring Security + JWT 实战复盘"));
        assertTrue(script.contains("前端面试经历复盘"));
        assertTrue(script.contains("MySQL 索引与慢查询排查清单"));

        assertTrue(script.contains("'前端'"));
        assertTrue(script.contains("'后端'"));
        assertTrue(script.contains("'数据库'"));
        assertTrue(script.contains("'组合式API'"));
        assertTrue(script.contains("'面试经历'"));
        assertTrue(script.contains("'项目复盘'"));
        assertTrue(script.contains("'索引'"));

        assertFalse(script.contains("Vue3 interview notes"));
        assertFalse(script.contains("Spring Security JWT recap"));
        assertFalse(script.contains("Frontend interview experience"));
        assertFalse(script.contains("MySQL index checklist"));
        assertFalse(script.contains("'Frontend'"));
        assertFalse(script.contains("'Backend'"));
        assertFalse(script.contains("'Database'"));
        assertFalse(script.contains("'Composition API'"));
        assertFalse(script.contains("'Interview'"));
        assertFalse(script.contains("'Project Review'"));
        assertFalse(script.contains("'Index'"));
    }
}
