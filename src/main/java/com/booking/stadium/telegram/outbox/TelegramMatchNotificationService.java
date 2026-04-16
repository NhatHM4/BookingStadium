package com.booking.stadium.telegram.outbox;

import com.booking.stadium.telegram.config.TelegramProperties;
import com.booking.stadium.telegram.enums.NotificationOutboxStatus;
import com.booking.stadium.telegram.event.MatchRequestCreatedEvent;
import com.booking.stadium.telegram.subscription.TelegramSubscription;
import com.booking.stadium.telegram.subscription.TelegramSubscriptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TelegramMatchNotificationService {

    private final TelegramSubscriptionService subscriptionService;
    private final TelegramNotificationOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final TelegramProperties properties;

    public TelegramMatchNotificationService(TelegramSubscriptionService subscriptionService,
                                            TelegramNotificationOutboxRepository outboxRepository,
                                            ObjectMapper objectMapper,
                                            TelegramProperties properties) {
        this.subscriptionService = subscriptionService;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Transactional
    public void enqueueMatchCreated(MatchRequestCreatedEvent event) {
        if (!properties.isBotEnabled()) {
            return;
        }
        List<TelegramSubscription> subscribers = subscriptionService.getActiveSubscribers();
        if (subscribers.isEmpty()) {
            return;
        }

        String payloadJson = toJson(event);
        for (TelegramSubscription subscription : subscribers) {
            TelegramNotificationOutbox outbox = TelegramNotificationOutbox.builder()
                    .eventType("MATCH_REQUEST_CREATED")
                    .eventId(event.getEventId())
                    .chatId(subscription.getChatId())
                    .payloadJson(payloadJson)
                    .status(NotificationOutboxStatus.PENDING)
                    .retryCount(0)
                    .nextRetryAt(LocalDateTime.now())
                    .build();
            try {
                outboxRepository.save(outbox);
            } catch (DataIntegrityViolationException ex) {
                log.debug("Skip duplicate outbox eventId={} chatId={}", event.getEventId(), subscription.getChatId());
            }
        }
    }

    private String toJson(MatchRequestCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize match event", e);
        }
    }
}
