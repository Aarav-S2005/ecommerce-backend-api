package com.aarav.ecommercebackend.ai.dto;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSessionDTO {
    private UUID conversationId;
}
