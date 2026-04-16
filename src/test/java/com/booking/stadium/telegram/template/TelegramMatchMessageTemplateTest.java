package com.booking.stadium.telegram.template;

import com.booking.stadium.telegram.event.MatchRequestCreatedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TelegramMatchMessageTemplateTest {

    private final TelegramMatchMessageTemplate template = new TelegramMatchMessageTemplate();

    @Test
    void renderNewMatch_shouldContainMainSections() {
        MatchRequestCreatedEvent event = MatchRequestCreatedEvent.builder()
                .eventId("evt-1")
                .matchRequestId(12L)
                .matchCode("M20260416ABCD")
                .stadiumName("Sân A")
                .fieldName("Sân 7-1")
                .bookingDate(LocalDate.of(2026, 4, 20))
                .startTime(LocalTime.of(19, 0))
                .endTime(LocalTime.of(20, 30))
                .hostTeamName("Đội Minh")
                .contactPhone("+84901234567")
                .build();

        String message = template.renderNewMatch(event, "https://booking.example.com/");
        assertTrue(message.contains("Kèo mới"));
        assertTrue(message.contains("Mã kèo: M20260416ABCD"));
        assertTrue(message.contains("Xem chi tiết: https://booking.example.com/matches/12"));
    }
}
