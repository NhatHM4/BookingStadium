package com.booking.stadium.telegram.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelegramRateLimitService {

    private static final int MAX_REQUEST_PER_MINUTE = 30;
    private static final long WINDOW_SECONDS = 60;

    private final Map<Long, CounterWindow> counterByChat = new ConcurrentHashMap<>();

    public boolean allow(Long chatId) {
        long now = Instant.now().getEpochSecond();
        CounterWindow current = counterByChat.compute(chatId, (id, existing) -> {
            if (existing == null || now - existing.windowStart > WINDOW_SECONDS) {
                return new CounterWindow(now, 1);
            }
            existing.count++;
            return existing;
        });
        return current.count <= MAX_REQUEST_PER_MINUTE;
    }

    private static final class CounterWindow {
        long windowStart;
        int count;

        private CounterWindow(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
