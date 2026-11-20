package com.renat.ollama.config;

import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenChatClientConfig {

    @Bean("openChatClient")
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
                                 SemanticCacheAdvisor semanticCacheAdvisor) {

        return chatClientBuilder.defaultAdvisors(List.of(semanticCacheAdvisor))
                .build();
    }

}
