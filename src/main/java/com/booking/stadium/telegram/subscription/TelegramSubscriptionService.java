package com.booking.stadium.telegram.subscription;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TelegramSubscriptionService {

    private final TelegramSubscriptionRepository subscriptionRepository;

    public TelegramSubscriptionService(TelegramSubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public void subscribeMatchAlert(Long chatId, Long telegramUserId) {
        TelegramSubscription subscription = subscriptionRepository.findByChatId(chatId)
                .orElse(TelegramSubscription.builder()
                        .chatId(chatId)
                        .build());
        subscription.setUserId(telegramUserId);
        subscription.setMatchAlertEnabled(true);
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void unsubscribeMatchAlert(Long chatId) {
        subscriptionRepository.findByChatId(chatId).ifPresent(subscription -> {
            subscription.setMatchAlertEnabled(false);
            subscriptionRepository.save(subscription);
        });
    }

    @Transactional(readOnly = true)
    public List<TelegramSubscription> getActiveSubscribers() {
        return subscriptionRepository.findByMatchAlertEnabledTrue();
    }
}
