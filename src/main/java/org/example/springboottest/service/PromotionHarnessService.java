package org.example.springboottest.service;

import org.example.springboottest.harness.tool.PromotionRequestTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class PromotionHarnessService {
    private static final Logger log = LoggerFactory.getLogger(PromotionHarnessService.class);

    private final ChatClient client;

    /**
     * Constructs a PromotionHarnessService with the auto-configured
     * ChatClient.Builder and PromotionRequest Tools.
     * @param builder Injected.
     * @param prTools Injected.
     */
    public PromotionHarnessService(ChatClient.Builder builder, PromotionRequestTools prTools) {
        // Only the Ollama starter is on the classpath, so there is a single
        // ChatModel bean and the auto-configured ChatClient.Builder is
        // unambiguous.  The model name comes from application properties,
        // which the auto-configurator passes to the ChatModel:
        // spring.ai.ollama.base-url=http://localhost:11434
        // spring.ai.ollama.chat.options.model=qwen2.5:14b
        this.client = builder
                .defaultSystem("You are an Employee Promotion Request expert.  Your job is to evaluate an employee and determine if they are to be promoted.")
                .defaultTools(prTools)
                .build();

        // To use the mock instead, no model is needed:
//        this.client = new ChatClientMockups.MockChatClient(prTools);
    }

    /**
     * Send the prompt to the model, which should realize that it needs to call
     * its tool.  The tool will send back the response object and then the
     * model will form its textual response using it.
     * @param userPrompt The user's prompt.
     * @return The model's response, hopefully containing the promotion status response.
     */
    public String execEmployeePromotionStatusRequestTask(String userPrompt) {
        log.info("Prompt received: {}", userPrompt);
        return client.prompt()
                .user(userPrompt)
                .call()
                .content();
    }
}
