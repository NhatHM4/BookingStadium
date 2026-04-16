package com.booking.stadium.telegram.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TelegramSubscriptionRepository extends JpaRepository<TelegramSubscription, Long> {

    Optional<TelegramSubscription> findByChatId(Long chatId);

    List<TelegramSubscription> findByMatchAlertEnabledTrue();
}
