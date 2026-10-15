package com.aarav.ecommercebackend.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.ai")
@Getter
@Setter
public class AiProperties {
    private int maxToolCalling = 5;
    private int maxHistoryMessages = 10;
}
