package com.booking.stadium.telegram.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TelegramIncomingMessage {
    Long updateId;
    Long chatId;
    Long telegramUserId;
    String username;
    String text;
}
