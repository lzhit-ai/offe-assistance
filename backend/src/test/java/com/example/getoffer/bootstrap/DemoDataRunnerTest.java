package com.example.getoffer.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

class DemoDataRunnerTest {

    @Test
    void demoDataRunnerRequiresExplicitOptIn() {
        ConditionalOnProperty annotation = DemoDataRunner.class.getAnnotation(ConditionalOnProperty.class);

        assertEquals("app.seed-demo-data", annotation.name()[0]);
        assertEquals("true", annotation.havingValue());
        assertFalse(annotation.matchIfMissing());
    }
}
