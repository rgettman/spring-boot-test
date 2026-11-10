package org.example.springboottest.service;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.example.springboottest.harness.dto.PromotionStatusRequest;
import org.example.springboottest.harness.dto.PromotionStatusResponse;
import org.example.springboottest.harness.tool.PromotionRequestTools;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.ai.template.TemplateRenderer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;

/**
 * Mock up some Chat Client related classes, so I don't have to get a separate
 * developer account with a separate payment method, just to test harnesses!
 */
public class ChatClientMockups {
    /**
     * Mock Chat Client just passes the prompt to the Mock Chat Client Request Spec.
     */
    public static class MockChatClient implements ChatClient {
        private final PromotionRequestTools tools;

        public MockChatClient(PromotionRequestTools tools) {
            this.tools = tools;
        }

        @Override
        public @NonNull ChatClientRequestSpec prompt() {
            return new MockChatClientRequestSpec(tools);
        }

        @Override
        public @NonNull ChatClientRequestSpec prompt(@NonNull String content) {
            return prompt().user(content);
        }

        @Override
        public @NonNull ChatClientRequestSpec prompt(@NonNull Prompt prompt) {
            throw new UnsupportedOperationException("Send a string prompt instead.");
        }

        @Override
        public @NonNull Builder mutate() {
            throw new UnsupportedOperationException("What does mutating a ChatClient even mean?");
        }
    }

    /**
     * Mock Chat Client Request Spec takes the prompt and gets a Mock Call Response Spec.
     */
    public static class MockChatClientRequestSpec implements ChatClient.ChatClientRequestSpec {
        private final PromotionRequestTools tools;
        private String text;

        public MockChatClientRequestSpec(PromotionRequestTools tools) {
            this.tools = tools;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec user(@NonNull String text) {
            this.text = text;
            return this;
        }

        /**
         * Simulate by expecting a string:
         * Employee [id] title [title]
         * @return The response.
         */
        @Override
        public ChatClient.@NonNull CallResponseSpec call() {
            String[] fields = text.split("\\s+", 4);
            if (fields.length != 4) {
                return new MockCallResponseSpec("""
                    I only understand prompts of the form: "Employee &lt;id&gt; title &lt;title&gt;".
                    """);
            }
            int employeeId;
            try {
                employeeId = Integer.parseInt(fields[1]);
            }
            catch (NumberFormatException e) {
                return new MockCallResponseSpec("Bad non-numeric employee ID: " + fields[1]);
            }
            String title = fields[3];

            PromotionStatusRequest request = new PromotionStatusRequest(employeeId, title);
            PromotionStatusResponse response = tools.checkPromotionStatus(request);

            // Mocking the LLM, convert the Response object into a textual response.
            StringBuilder builder = new StringBuilder();
            builder.append("The request for employee with ID ").append(response.employeeId())
                    .append(" to be promoted to \"").append(response.desiredTitle())
                    .append("\" has ");
            if (!response.approved()) {
                builder.append("NOT ");
            }
            builder.append("been approved.  Reason: ").append(response.reason());
            return new MockCallResponseSpec(builder.toString());
        }

        // Not going to call these methods below.

        @Override
        public ChatClient.@NonNull Builder mutate() {
            return null;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec advisors(@NonNull Consumer<ChatClient.AdvisorSpec> consumer) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec advisors(Advisor @NonNull ... advisors) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec advisors(@NonNull List<Advisor> advisors) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec messages(Message @NonNull ... messages) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec messages(@NonNull List<Message> messages) {
            return this;
        }

        @Override
        public <B extends ChatOptions.Builder<?>> ChatClient.@NonNull ChatClientRequestSpec options(B customizer) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec tools(Object @NonNull ... tools) {
            return this;
        }

        @Override
        public ChatClient.ChatClientRequestSpec toolCallbacks(ToolCallback @NonNull ... toolCallbacks) {
            return this;
        }

        @Override
        public ChatClient.ChatClientRequestSpec toolCallbacks(@NonNull List<ToolCallback> toolCallbacks) {
            return this;
        }

        @Override
        public ChatClient.ChatClientRequestSpec toolCallbacks(ToolCallbackProvider @NonNull ... toolCallbackProviders) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec toolContext(@NonNull Map<String, Object> toolContext) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec system(@NonNull String text) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec system(@NonNull Resource textResource, @NonNull Charset charset) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec system(@NonNull Resource text) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec system(@NonNull Consumer<ChatClient.PromptSystemSpec> consumer) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec user(@NonNull Resource text, @NonNull Charset charset) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec user(@NonNull Resource text) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec user(@NonNull Consumer<ChatClient.PromptUserSpec> consumer) {
            return this;
        }

        @Override
        public ChatClient.@NonNull ChatClientRequestSpec templateRenderer(@NonNull TemplateRenderer templateRenderer) {
            return this;
        }

        @Override
        public ChatClient.@NonNull StreamResponseSpec stream() {
            return null;
        }
    }

    /**
     * Mock Chat Client Response Spec takes the response and can get the content back.
     */
    public static class MockCallResponseSpec implements ChatClient.CallResponseSpec {
        private final String response;

        public MockCallResponseSpec(String response) {
            this.response = response;
        }

        @Override
        public @Nullable String content() {
            return response;
        }

        // Not going to call these methods below.

        @Override
        public @Nullable <T> T entity(@NonNull ParameterizedTypeReference<T> type, @NonNull Consumer<ChatClient.EntityParamSpec> entityParamSpecConsumer) {
            return null;
        }

        @Override
        public @Nullable <T> T entity(@NonNull ParameterizedTypeReference<T> type) {
            return null;
        }

        @Override
        public @Nullable <T> T entity(@NonNull StructuredOutputConverter<T> structuredOutputConverter, @NonNull Consumer<ChatClient.EntityParamSpec> entityParamSpecConsumer) {
            return null;
        }

        @Override
        public @Nullable <T> T entity(@NonNull StructuredOutputConverter<T> structuredOutputConverter) {
            return null;
        }

        @Override
        public @Nullable <T> T entity(@NonNull Class<T> type, @NonNull Consumer<ChatClient.EntityParamSpec> entityParamSpecConsumer) {
            return null;
        }

        @Override
        public @Nullable <T> T entity(@NonNull Class<T> type) {
            return null;
        }

        @Override
        public @NonNull ChatClientResponse chatClientResponse() {
            return null;
        }

        @Override
        public @Nullable ChatResponse chatResponse() {
            return null;
        }

        @Override
        public <T> @NonNull ResponseEntity<ChatResponse, T> responseEntity(@NonNull Class<T> type, @NonNull Consumer<ChatClient.EntityParamSpec> entityParamSpecConsumer) {
            return null;
        }

        @Override
        public <T> @NonNull ResponseEntity<ChatResponse, T> responseEntity(@NonNull Class<T> type) {
            return null;
        }

        @Override
        public <T> @NonNull ResponseEntity<ChatResponse, T> responseEntity(@NonNull ParameterizedTypeReference<T> type, @NonNull Consumer<ChatClient.EntityParamSpec> entityParamSpecConsumer) {
            return null;
        }

        @Override
        public <T> @NonNull ResponseEntity<ChatResponse, T> responseEntity(@NonNull ParameterizedTypeReference<T> type) {
            return null;
        }

        @Override
        public <T> @NonNull ResponseEntity<ChatResponse, T> responseEntity(@NonNull StructuredOutputConverter<T> structuredOutputConverter, @NonNull Consumer<ChatClient.EntityParamSpec> entityParamSpecConsumer) {
            return null;
        }

        @Override
        public <T> @NonNull ResponseEntity<ChatResponse, T> responseEntity(@NonNull StructuredOutputConverter<T> structuredOutputConverter) {
            return null;
        }
    }
}
