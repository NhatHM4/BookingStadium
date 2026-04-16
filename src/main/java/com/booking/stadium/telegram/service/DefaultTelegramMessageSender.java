package com.booking.stadium.telegram.service;

import com.booking.stadium.telegram.config.TelegramProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DefaultTelegramMessageSender implements TelegramMessageSender {

    private final RestTemplate restTemplate;
    private final TelegramProperties properties;

    public DefaultTelegramMessageSender(RestTemplateBuilder restTemplateBuilder, TelegramProperties properties) {
        this.restTemplate = restTemplateBuilder.build();
        this.properties = properties;
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        if (!properties.isBotEnabled() || properties.getBotToken() == null || properties.getBotToken().isBlank()) {
            return;
        }

        String url = "https://api.telegram.org/bot" + properties.getBotToken() + "/sendMessage";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", text);
        payload.put("disable_web_page_preview", true);

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), JsonNode.class);
            JsonNode body = response.getBody();
            if (body == null || !body.path("ok").asBoolean(false)) {
                Integer retryAfter = body == null ? null : body.path("parameters").path("retry_after").isInt()
                        ? body.path("parameters").path("retry_after").asInt()
                        : null;
                String description = body == null ? "empty response body" : body.path("description").asText("unknown");
                if (response.getStatusCode().value() == 429 || retryAfter != null) {
                    throw new TelegramRateLimitException("Telegram rate limit: " + description, retryAfter);
                }
                throw new TelegramSendException("Telegram send failed: " + description);
            }
        } catch (RestClientException ex) {
            log.warn("Telegram send error for chatId={}: {}", chatId, ex.getMessage());
            throw new TelegramSendException("Không gửi được tin nhắn Telegram", ex);
        }
    }
}
