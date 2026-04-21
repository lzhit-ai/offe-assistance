package com.example.getoffer.service.ai;

import java.util.List;
import java.util.function.Consumer;

public interface DeepSeekClient {

    void streamChat(List<AiModelMessage> messages, Consumer<String> chunkConsumer);
}
