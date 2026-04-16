package com.booking.stadium.telegram.outbox;

import com.booking.stadium.telegram.config.TelegramProperties;
import com.booking.stadium.telegram.conversation.TelegramConversationRepository;
import com.booking.stadium.telegram.enums.NotificationOutboxStatus;
import com.booking.stadium.telegram.event.MatchRequestCreatedEvent;
import com.booking.stadium.telegram.service.TelegramMessageSender;
import com.booking.stadium.telegram.service.TelegramRateLimitException;
import com.booking.stadium.telegram.service.TelegramSendException;
import com.booking.stadium.telegram.template.TelegramMatchMessageTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class TelegramOutboxWorker {

    private final TelegramNotificationOutboxRepository outboxRepository;
    private final TelegramMessageSender messageSender;
    private final TelegramMatchMessageTemplate messageTemplate;
    private final ObjectMapper objectMapper;
    private final TelegramProperties properties;
    private final TelegramOutboxMetrics metrics;
    private final TelegramConversationRepository conversationRepository;

    public TelegramOutboxWorker(TelegramNotificationOutboxRepository outboxRepository,
                                TelegramMessageSender messageSender,
                                TelegramMatchMessageTemplate messageTemplate,
                                ObjectMapper objectMapper,
                                TelegramProperties properties,
                                TelegramOutboxMetrics metrics,
                                TelegramConversationRepository conversationRepository) {
        this.outboxRepository = outboxRepository;
        this.messageSender = messageSender;
        this.messageTemplate = messageTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.metrics = metrics;
        this.conversationRepository = conversationRepository;
    }

    @Scheduled(fixedDelayString = "${app.telegram.worker-fixed-delay-ms:10000}")
    @Transactional
    public void process() {
        metrics.setActiveConversationCount(conversationRepository.countByExpiresAtAfter(LocalDateTime.now()));
        if (!properties.isBotEnabled()) {
            return;
        }

        List<TelegramNotificationOutbox> batch = outboxRepository.findReadyForDispatch(
                List.of(NotificationOutboxStatus.PENDING, NotificationOutboxStatus.FAILED),
                LocalDateTime.now());
        batch.stream().limit(50).forEach(this::dispatchOne);
    }

    private void dispatchOne(TelegramNotificationOutbox outbox) {
        try {
            MatchRequestCreatedEvent event = objectMapper.readValue(outbox.getPayloadJson(), MatchRequestCreatedEvent.class);
            String text = messageTemplate.renderNewMatch(event, properties.getAppBaseUrl());
            messageSender.sendMessage(outbox.getChatId(), text);

            outbox.setStatus(NotificationOutboxStatus.SENT);
            outbox.setLastError(null);
            outbox.setNextRetryAt(null);
            outboxRepository.save(outbox);
            metrics.incrementSent();
        } catch (TelegramRateLimitException ex) {
            markFailed(outbox, ex.getMessage(), ex.getRetryAfterSeconds());
        } catch (TelegramSendException ex) {
            markFailed(outbox, ex.getMessage(), null);
        } catch (Exception ex) {
            markFailed(outbox, ex.getMessage(), null);
        }
    }

    private void markFailed(TelegramNotificationOutbox outbox, String error, Integer retryAfterSeconds) {
        int retryCount = outbox.getRetryCount() + 1;
        outbox.setRetryCount(retryCount);
        outbox.setLastError(trim(error));
        if (retryCount > properties.getMaxRetry()) {
            outbox.setStatus(NotificationOutboxStatus.DEAD);
            outbox.setNextRetryAt(null);
        } else {
            outbox.setStatus(NotificationOutboxStatus.FAILED);
            long waitSeconds = retryAfterSeconds != null
                    ? retryAfterSeconds
                    : (long) Math.min(300, Math.pow(2, retryCount) * 5);
            outbox.setNextRetryAt(LocalDateTime.now().plusSeconds(waitSeconds));
        }
        outboxRepository.save(outbox);
        metrics.incrementFailed();
        log.warn("Telegram outbox failed id={} retry={} status={} error={}",
                outbox.getId(), retryCount, outbox.getStatus(), outbox.getLastError());
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 450 ? value.substring(0, 450) : value;
    }
}
