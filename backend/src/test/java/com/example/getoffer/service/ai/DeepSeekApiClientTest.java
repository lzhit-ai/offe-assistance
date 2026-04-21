package com.example.getoffer.service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.junit.jupiter.api.Test;

class DeepSeekApiClientTest {

    @Test
    void buildChatCompletionsUriAppendsV1ForPlainBaseUrl() {
        URI uri = DeepSeekApiClient.buildChatCompletionsUri("https://openrouter.fans");

        assertEquals("https://openrouter.fans/v1/chat/completions", uri.toString());
    }

    @Test
    void buildChatCompletionsUriAvoidsDuplicatingV1() {
        URI uri = DeepSeekApiClient.buildChatCompletionsUri("https://openrouter.fans/v1/");

        assertEquals("https://openrouter.fans/v1/chat/completions", uri.toString());
    }
}
