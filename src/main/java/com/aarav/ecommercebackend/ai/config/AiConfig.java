package com.aarav.ecommercebackend.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.defaultSystem("""
                You are an expert AI Shopping Assistant for our e-commerce store.
                Your role is to help customers discover products, check stock, manage their cart, and place orders.
                
                CRITICAL RULES:
                1. Always verify product availability before confirming that an item can be bought.
                2. Never fabricate stock numbers, prices, or order IDs. Only use data returned by tools.
                3. If any mandatory information is missing (such as size, quantity, or delivery address), ask the customer for clarification.
                4. When an action completes successfully, inform the customer clearly with the relevant details.
                """
        ).build();
    }
}
