package com.booking.stadium.telegram.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.telegram.outbox.TelegramOutboxMetrics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/telegram")
public class TelegramMetricsController {

    private final TelegramOutboxMetrics metrics;

    public TelegramMetricsController(TelegramOutboxMetrics metrics) {
        this.metrics = metrics;
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getMetrics() {
        Map<String, Long> data = Map.of(
                "telegram_sent_success", metrics.getSentCount(),
                "telegram_send_failed", metrics.getFailedCount(),
                "telegram_active_conversations", metrics.getActiveConversationCount()
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
