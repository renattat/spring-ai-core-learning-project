package com.renat.ollama.controller;

import com.renat.ollama.model.CountryCities;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StructuredOutputController {

    private final ChatClient chatClient;

    public StructuredOutputController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping("/chat-bean")
    public ResponseEntity<CountryCities> chatBean(@RequestParam("message") String message) {
        CountryCities countryCities = chatClient
                .prompt()
                .system("You must return a SINGLE JSON object strictly matching the provided schema. DO NOT wrap the result in a JSON array. Your response must start with '{' and end with '}'.")
                .user(message)
                .call().entity(CountryCities.class);
//                .call().entity(new BeanOutputConverter<>(CountryCities.class));
        return ResponseEntity.ok(countryCities);
    }

    @GetMapping("/chat-list")
    public ResponseEntity<List<String>> chatList(@RequestParam("message") String message) {
        List<String> countryCities = chatClient
                .prompt()
                .user(message)
                .options(OllamaChatOptions.builder()
                        .model(OllamaModel.LLAMA3_2)
                        .temperature(0.1)
                        .numPredict(300)
                        .topK(40))
                .call().entity(new ListOutputConverter());
        return ResponseEntity.ok(countryCities);
    }

    @GetMapping("/chat-map")
    public ResponseEntity<Map<String, Object>> chatMap(@RequestParam("message") String message) {
        String enhancedPrompt = message + "\n\n" +
                "CRITICAL: Return strictly as a SINGLE valid JSON object. " +
                "Provide details for EXACTLY 3 cities MAXIMUM to keep the response short. " +
                "KEYS must be city names (strings). VALUES must be objects with: state, population, area, city_type. " +
                "DO NOT return an array. DO NOT use markdown formatting. Return ONLY raw JSON.";


        Map<String, Object> countryCities = chatClient.prompt()
                .user(enhancedPrompt)
                .options(OllamaChatOptions.builder()
                        .model(OllamaModel.LLAMA3_2)
                        .temperature(0.1)
                        .numPredict(800)
                        .topK(40))
                .call()
                .entity(new MapOutputConverter());
        return ResponseEntity.ok(countryCities);
    }
}
