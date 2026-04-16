package com.booking.stadium.telegram.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TelegramInputValidatorTest {

    private final TelegramInputValidator validator = new TelegramInputValidator();

    @Test
    void parseDate_shouldRejectWrongFormat() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.parseDate("2026/10/01"));
        assertTrue(ex.getMessage().contains("YYYY-MM-DD"));
    }

    @Test
    void parseTime_shouldRejectPastTime() {
        LocalDate today = LocalDate.now();
        LocalTime oneHourAgo = LocalTime.now().minusHours(1);
        assertThrows(IllegalArgumentException.class,
                () -> validator.parseTime(oneHourAgo.toString(), today));
    }

    @Test
    void normalizePhone_shouldConvertLocalFormat() {
        String normalized = validator.normalizePhone("0912 345 678");
        assertEquals("+84912345678", normalized);
    }
}
