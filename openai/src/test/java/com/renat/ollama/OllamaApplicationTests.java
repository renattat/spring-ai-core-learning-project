package com.renat.ollama;

import com.renat.ollama.controller.ChatTestController;
import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {"logging.level.org.springframework.ai=DEBUG"})
class OllamaApplicationTests {


    @Autowired
    private ChatTestController chatController;

    @Autowired
    private ChatModel chatModel;

    private ChatClient chatClient;
    private RelevancyEvaluator relevancyEvaluator;

    // Minimum acceptable relevancy score
    @Value("${test.relevancy-score:0.7}")
    private float minRelevancyScore;


    @BeforeEach
    void setup() {
        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor());
        this.chatClient = chatClientBuilder.build();
        // Кастомный промпт, который ЗАПРЕЩАЕТ модели ставить точку или добавлять слова
        PromptTemplate evaluatorPrompt = new PromptTemplate("""
            Your task is to evaluate if the response for the query is correct.
            You must answer with EXACTLY the word YES or NO. 
            Do not add any punctuation, periods, or extra text.
            
            Query:
            {query}
            
            Response:
            {response}
            
            Context:
            {context}
            
            Answer:
            """);

        this.relevancyEvaluator = RelevancyEvaluator.builder()
                .chatClientBuilder(chatClientBuilder)
                .promptTemplate(evaluatorPrompt)
                .build();
    }

    @Test
    @DisplayName("Should return relevant response for basic geography question")
    @Timeout(value = 30)
    void evaluateChatControllerResponseRelevancy() {
        // Given
        String question = "What is the capital of Russia ?";

        //When
        String aiResponse = chatController.chat(question);

        //Then
        EvaluationRequest evaluationRequest = new EvaluationRequest(question, aiResponse);
        EvaluationResponse response = relevancyEvaluator.evaluate(evaluationRequest);

        Assertions.assertAll(() -> assertThat(aiResponse).isNotBlank(),
                () -> assertThat(response.isPass())
                        .withFailMessage("""
                                =================
                                The answer was not considered relevant.
                                Question: "%s"
                                Response: "%s"
                                ================
                                """, question, aiResponse)
                        .isTrue(),
                () -> assertThat(response.getScore())
                        .withFailMessage(
                                """
                                        =================
                                        The score %.2f is lower than the minimum required %.2f.
                                        Question: "%s"
                                        Response: "%s"
                                        ================
                                        """, response.getScore(), minRelevancyScore, question, aiResponse
                        )
                        .isGreaterThan(minRelevancyScore));
    }
}
