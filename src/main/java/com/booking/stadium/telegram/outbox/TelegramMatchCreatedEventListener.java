package com.booking.stadium.telegram.outbox;

import com.booking.stadium.telegram.event.MatchRequestCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TelegramMatchCreatedEventListener {

    private final TelegramMatchNotificationService notificationService;

    public TelegramMatchCreatedEventListener(TelegramMatchNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onMatchCreated(MatchRequestCreatedEvent event) {
        notificationService.enqueueMatchCreated(event);
    }
}
