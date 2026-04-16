package com.booking.stadium.telegram.service;

import com.booking.stadium.telegram.conversation.TelegramConversation;
import com.booking.stadium.telegram.conversation.TelegramConversationService;
import com.booking.stadium.telegram.dto.TelegramIncomingMessage;
import com.booking.stadium.telegram.handlers.TelegramCommandHandler;
import com.booking.stadium.telegram.handlers.TelegramConversationHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TelegramUpdateDispatcher {

    private final TelegramUpdateParser updateParser;
    private final TelegramRateLimitService rateLimitService;
    private final TelegramCommandHandler commandHandler;
    private final TelegramConversationService conversationService;
    private final TelegramConversationHandler conversationHandler;
    private final TelegramMessageSender messageSender;

    public TelegramUpdateDispatcher(TelegramUpdateParser updateParser,
                                    TelegramRateLimitService rateLimitService,
                                    TelegramCommandHandler commandHandler,
                                    TelegramConversationService conversationService,
                                    TelegramConversationHandler conversationHandler,
                                    TelegramMessageSender messageSender) {
        this.updateParser = updateParser;
        this.rateLimitService = rateLimitService;
        this.commandHandler = commandHandler;
        this.conversationService = conversationService;
        this.conversationHandler = conversationHandler;
        this.messageSender = messageSender;
    }

    @Transactional
    public void dispatch(JsonNode rawUpdate) {
        TelegramIncomingMessage incomingMessage = updateParser.parse(rawUpdate);
        if (incomingMessage == null) {
            return;
        }

        if (!rateLimitService.allow(incomingMessage.getChatId())) {
            messageSender.sendMessage(incomingMessage.getChatId(), "Bạn thao tác hơi nhanh, vui lòng thử lại sau ít phút.");
            return;
        }

        if (commandHandler.handle(incomingMessage)) {
            return;
        }

        TelegramConversation conversation = conversationService.getOrCreate(incomingMessage.getChatId());
        if (conversationHandler.handle(incomingMessage, conversation)) {
            return;
        }
        messageSender.sendMessage(incomingMessage.getChatId(),
                "Mình chưa hiểu yêu cầu. Bạn dùng /start để xem lệnh hoặc /san_trong để tìm sân.");
    }
}
