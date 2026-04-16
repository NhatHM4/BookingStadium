package com.booking.stadium.telegram.outbox;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class TelegramOutboxMetrics {

    private final AtomicLong sentCount = new AtomicLong();
    private final AtomicLong failedCount = new AtomicLong();
    private final AtomicLong activeConversationCount = new AtomicLong();

    public void incrementSent() {
        sentCount.incrementAndGet();
    }

    public void incrementFailed() {
        failedCount.incrementAndGet();
    }

    public void setActiveConversationCount(long count) {
        activeConversationCount.set(count);
    }

    public long getSentCount() {
        return sentCount.get();
    }

    public long getFailedCount() {
        return failedCount.get();
    }

    public long getActiveConversationCount() {
        return activeConversationCount.get();
    }
}
