package com.booking.stadium.telegram.conversation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramConversationRepository extends JpaRepository<TelegramConversation, Long> {

    Optional<TelegramConversation> findByChatId(Long chatId);

    long countByExpiresAtAfter(java.time.LocalDateTime time);
}
