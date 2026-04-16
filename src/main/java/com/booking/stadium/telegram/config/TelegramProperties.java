package com.booking.stadium.telegram.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.telegram")
public class TelegramProperties {

    private boolean botEnabled;
    private String botToken;
    private String webhookSecret;
    private String appBaseUrl;
    private int conversationTimeoutMinutes = 15;
    private int maxRetry = 5;
    private long workerFixedDelayMs = 10000;
}
