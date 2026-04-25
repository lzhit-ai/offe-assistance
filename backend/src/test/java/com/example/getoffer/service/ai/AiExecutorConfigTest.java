package com.example.getoffer.service.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class AiExecutorConfigTest {

    @Test
    void aiExecutorServiceExecutesTasksOnJava17CompatibleExecutor() throws Exception {
        AiExecutorConfig config = new AiExecutorConfig();
        ExecutorService executorService = config.aiExecutorService();

        try {
            Future<String> result = executorService.submit(() -> "ok");
            assertThat(result.get(5, TimeUnit.SECONDS)).isEqualTo("ok");
        } finally {
            executorService.shutdownNow();
        }
    }
}
