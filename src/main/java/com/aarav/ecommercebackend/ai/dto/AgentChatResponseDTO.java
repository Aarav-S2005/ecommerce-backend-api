package com.aarav.ecommercebackend.ai.dto;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatResponseDTO {
    private UUID conversationId;
    private String message;
    private AgentResponseStatus status;
    private UiActionDTO uiAction;
    private Object data;
}
