package com.aarav.ecommercebackend.ai.orchestrator;

import com.aarav.ecommercebackend.ai.config.AiProperties;
import com.aarav.ecommercebackend.ai.dto.AgentChatRequestDTO;
import com.aarav.ecommercebackend.ai.dto.AgentChatResponseDTO;
import com.aarav.ecommercebackend.ai.dto.AgentResponseStatus;
import com.aarav.ecommercebackend.ai.dto.PageContextDTO;
import com.aarav.ecommercebackend.ai.dto.UiActionDTO;
import com.aarav.ecommercebackend.ai.dto.UiActionType;
import com.aarav.ecommercebackend.ai.tools.CartTools;
import com.aarav.ecommercebackend.ai.tools.CatalogTools;
import com.aarav.ecommercebackend.ai.tools.OrderTools;
import com.aarav.ecommercebackend.ai.tools.UserTools;
import com.aarav.ecommercebackend.security.SecurityUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentOrchestrator {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ObjectMapper objectMapper;

    // The 4 domain tools
    private final UserTools userTools;
    private final CatalogTools catalogTools;
    private final CartTools cartTools;
    private final OrderTools orderTools;

    private static final String ERROR_MESSAGE = "I'm sorry, I encountered an issue processing your request. Please try again in some time.";

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AgentReplyPayload(
            String message,
            AgentResponseStatus status,
            UiActionType uiAction,
            Object payload
    ) {}


    public AgentChatResponseDTO chat(AgentChatRequestDTO request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        UUID conversationId = request.getConversationId() != null ? request.getConversationId() : UUID.randomUUID();

        String memoryConversationKey = (currentUserId) + ":" + conversationId;

        String contextualUserPrompt = enrichPromptWithPageContext(request.getQuery(), request.getPageContext());

        log.info("Agent Turn: userId={}, conversationId={}, query='{}'", currentUserId, conversationId, request.getQuery());

        try {
            String rawAssistantOutput = chatClient.prompt()
                    .user(contextualUserPrompt)
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, memoryConversationKey))
                    .tools(userTools, catalogTools, cartTools, orderTools)
                    .call()
                    .content();

            return buildSafeResponse(conversationId, rawAssistantOutput);

        } catch (Exception ex) {
            log.error("Error during Agent execution for conversation {}: {}", conversationId, ex.getMessage(), ex);
            return AgentChatResponseDTO.builder()
                    .conversationId(conversationId)
                    .message(ERROR_MESSAGE)
                    .status(AgentResponseStatus.ERROR)
                    .uiAction(UiActionDTO.none())
                    .build();
        }
    }

    public void clearSession(UUID conversationId) {
        if (conversationId != null) {
            UUID currentUserId = SecurityUtils.getCurrentUserId();
            String memoryConversationKey = (currentUserId != null ? currentUserId : "anonymous")
                    + ":" + conversationId;
            chatMemory.clear(memoryConversationKey);
            log.info("Cleared chat memory for key: {}", memoryConversationKey);
        }
    }

    private AgentChatResponseDTO buildSafeResponse(UUID conversationId, String rawOutput) {
        if (rawOutput == null || rawOutput.isBlank()) {
            log.warn("Model returned null or blank response for conversation: {}", conversationId);
            return AgentChatResponseDTO.builder()
                    .conversationId(conversationId)
                    .message(ERROR_MESSAGE)
                    .status(AgentResponseStatus.ERROR)
                    .uiAction(UiActionDTO.none())
                    .build();
        }

        String cleaned = cleanJsonFences(rawOutput);

        try {
            AgentReplyPayload payload = objectMapper.readValue(cleaned, AgentReplyPayload.class);

            AgentResponseStatus resolvedStatus = payload.status() != null ? payload.status() : AgentResponseStatus.COMPLETED;

            UiActionType actionType = payload.uiAction() != null ? payload.uiAction() : UiActionType.NONE;

            UiActionDTO uiAction = UiActionDTO.of(actionType, payload.payload());

            return AgentChatResponseDTO.builder()
                    .conversationId(conversationId)
                    .message(payload.message() != null ? payload.message() : cleaned)
                    .status(resolvedStatus)
                    .uiAction(uiAction)
                    .build();

        } catch (Exception ex) {
            log.warn("Model output was not valid JSON, returning plain text message with COMPLETED status. Raw output: '{}'", rawOutput);
            return AgentChatResponseDTO.builder()
                    .conversationId(conversationId)
                    .message(rawOutput)
                    .status(AgentResponseStatus.COMPLETED)
                    .uiAction(UiActionDTO.none())
                    .build();
        }
    }

    private String cleanJsonFences(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private String enrichPromptWithPageContext(String query, PageContextDTO pageContext) {
        if (pageContext == null || pageContext.getPageType() == null) {
            return query;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[PAGE_CONTEXT: pageType=").append(pageContext.getPageType());
        if (pageContext.getProductId() != null) {
            sb.append(", productId=").append(pageContext.getProductId());
        }
        if (pageContext.getCategoryId() != null) {
            sb.append(", categoryId=").append(pageContext.getCategoryId());
        }
        if (pageContext.getSearchQuery() != null && !pageContext.getSearchQuery().isBlank()) {
            sb.append(", activeSearch='").append(pageContext.getSearchQuery()).append("'");
        }
        sb.append("]\n");
        sb.append(query);

        return sb.toString();
    }
}
