package com.renat.ollama.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PromptStuffingController {


    private final ChatClient chatClient;

    public PromptStuffingController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Value("classpath:/promptTemplate/systemPromtTemplate.st")
    Resource systemPromptTemplate;

    @GetMapping("/promt-stuffing")
    public String emailResponse(@RequestParam("message") String message) {
        return chatClient
                .prompt()
                .options(OllamaChatOptions.builder().temperature(0.7))
                .system(systemPromptTemplate)
                .user(message)
                .call()
                .content();
    }
}
