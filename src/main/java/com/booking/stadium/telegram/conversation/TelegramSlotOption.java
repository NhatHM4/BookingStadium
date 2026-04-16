package com.booking.stadium.telegram.conversation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramSlotOption {
    private Long slotId;
    private Long fieldId;
    private String stadiumName;
    private String fieldName;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
}
