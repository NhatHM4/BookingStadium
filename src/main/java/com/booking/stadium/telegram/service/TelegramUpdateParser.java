package com.booking.stadium.telegram.service;

import com.booking.stadium.telegram.dto.TelegramIncomingMessage;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class TelegramUpdateParser {

    public TelegramIncomingMessage parse(JsonNode update) {
        if (update == null) {
            return null;
        }
        JsonNode messageNode = update.path("message");
        if (messageNode.isMissingNode()) {
            messageNode = update.path("edited_message");
        }
        if (messageNode.isMissingNode()) {
            return null;
        }

        String text = messageNode.path("text").asText(null);
        Long chatId = messageNode.path("chat").path("id").isNumber()
                ? messageNode.path("chat").path("id").asLong()
                : null;
        Long userId = messageNode.path("from").path("id").isNumber()
                ? messageNode.path("from").path("id").asLong()
                : null;
        String username = messageNode.path("from").path("username").asText(null);
        Long updateId = update.path("update_id").isNumber() ? update.path("update_id").asLong() : null;

        if (chatId == null || text == null || text.isBlank()) {
            return null;
        }

        return TelegramIncomingMessage.builder()
                .updateId(updateId)
                .chatId(chatId)
                .telegramUserId(userId)
                .username(username)
                .text(text.trim())
                .build();
    }
}
