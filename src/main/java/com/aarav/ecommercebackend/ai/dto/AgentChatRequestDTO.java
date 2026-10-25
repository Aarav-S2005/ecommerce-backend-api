package com.aarav.ecommercebackend.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatRequestDTO {
    private UUID conversationId;
    @NotBlank(message = "query cannot be blank")
    private String query;
    private PageContextDTO pageContext;
}
