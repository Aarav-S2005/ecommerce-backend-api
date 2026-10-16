package com.aarav.ecommercebackend.ai.controller;

import com.aarav.ecommercebackend.ai.dto.AgentChatRequestDTO;
import com.aarav.ecommercebackend.ai.dto.AgentChatResponseDTO;
import com.aarav.ecommercebackend.ai.dto.ConversationSessionDTO;
import com.aarav.ecommercebackend.ai.orchestrator.AgentOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AgentController {
    private final AgentOrchestrator agentOrchestrator;

    @PostMapping("/chat")
    public ResponseEntity<AgentChatResponseDTO> chat(
            @Valid @RequestBody AgentChatRequestDTO request
    ) {
        AgentChatResponseDTO response = agentOrchestrator.chat(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/new-conversation")
    public ResponseEntity<ConversationSessionDTO> startNewConversation() {
        UUID newConversationId = UUID.randomUUID();
        return ResponseEntity.ok(
                ConversationSessionDTO.builder()
                        .conversationId(newConversationId)
                        .build()
        );
    }

    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<Void> clearConversation(
            @PathVariable UUID conversationId
    ) {
        agentOrchestrator.clearSession(conversationId);
        return ResponseEntity.noContent().build();
    }
}
