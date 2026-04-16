package com.booking.stadium.telegram.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.telegram.config.TelegramProperties;
import com.booking.stadium.telegram.service.TelegramUpdateDispatcher;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/telegram")
public class TelegramWebhookController {

    private final TelegramProperties telegramProperties;
    private final TelegramUpdateDispatcher updateDispatcher;

    public TelegramWebhookController(TelegramProperties telegramProperties,
                                     TelegramUpdateDispatcher updateDispatcher) {
        this.telegramProperties = telegramProperties;
        this.updateDispatcher = updateDispatcher;
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<String>> webhook(
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secret,
            @RequestBody(required = false) JsonNode update) {
        if (!telegramProperties.isBotEnabled()) {
            return ResponseEntity.ok(ApiResponse.success("Telegram bot disabled"));
        }
        if (telegramProperties.getWebhookSecret() != null
                && !telegramProperties.getWebhookSecret().isBlank()
                && !telegramProperties.getWebhookSecret().equals(secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Webhook secret không hợp lệ"));
        }
        updateDispatcher.dispatch(update);
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }
}
