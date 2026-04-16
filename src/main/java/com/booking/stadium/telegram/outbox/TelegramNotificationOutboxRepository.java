package com.booking.stadium.telegram.outbox;

import com.booking.stadium.telegram.enums.NotificationOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TelegramNotificationOutboxRepository extends JpaRepository<TelegramNotificationOutbox, Long> {

    boolean existsByEventIdAndChatId(String eventId, Long chatId);

    @Query("""
            SELECT o FROM TelegramNotificationOutbox o
            WHERE o.status IN :statuses
              AND (o.nextRetryAt IS NULL OR o.nextRetryAt <= :now)
            ORDER BY o.createdAt ASC
            """)
    List<TelegramNotificationOutbox> findReadyForDispatch(
            @Param("statuses") List<NotificationOutboxStatus> statuses,
            @Param("now") LocalDateTime now);
}
