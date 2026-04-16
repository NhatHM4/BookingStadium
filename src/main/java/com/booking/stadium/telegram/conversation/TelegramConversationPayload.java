package com.booking.stadium.telegram.conversation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramConversationPayload {
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private String district;
    @Builder.Default
    private List<TelegramSlotOption> slotOptions = new ArrayList<>();
    private TelegramSlotOption selectedSlot;
    private String phone;
    private String bookerName;
}
