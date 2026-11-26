package com.renat.ollama.controller;

import com.renat.ollama.exception.InvalidAnswerException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@RestController
@RequestMapping("test/api")
public class SelfEvaluatingChatController {

    private final ChatClient chatClient;
    private final FactCheckingEvaluator factCheckingEvaluator;

    @Value("classpath:/promptTemplate/hrPolicy.st")
    Resource hrPolicyTemplate;

    public SelfEvaluatingChatController(ChatClient.Builder chatClientBuilder,
                                        @Value("classpath:/promptTemplate/factcheck.st") Resource factCheckingEvaluator) throws IOException {
        this.chatClient = chatClientBuilder.defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.factCheckingEvaluator = FactCheckingEvaluator
                .builder(chatClientBuilder)
                .evaluationPrompt(factCheckingEvaluator.getContentAsString(Charset.defaultCharset()))
                .build();
    }

    @GetMapping("/evaluate/chat")
    public String chat(@RequestParam("message") String message) {
        String aiResponse = chatClient.prompt().user(message)
                .call().content();
        validateAnswer(message, aiResponse);
        return aiResponse;
    }

    @GetMapping("/evaluate/prompt-stuffing")
    public String promptStuffing(@RequestParam("message") String message) {
        return chatClient
                .prompt().system(hrPolicyTemplate)
                .user(message)
                .call().content();
    }

    private void validateAnswer(String message, String answer) {
        EvaluationRequest evaluationRequest = new EvaluationRequest(message, List.of(), answer);
        EvaluationResponse evaluationResponse = factCheckingEvaluator.evaluate(evaluationRequest);
        if (!evaluationResponse.isPass()) {
            throw new InvalidAnswerException(message, answer);
        }
    }

}
