package com.booking.stadium.telegram.handlers;

import com.booking.stadium.telegram.conversation.TelegramConversationPayload;
import com.booking.stadium.telegram.conversation.TelegramConversationService;
import com.booking.stadium.telegram.dto.TelegramIncomingMessage;
import com.booking.stadium.telegram.enums.TelegramConversationState;
import com.booking.stadium.telegram.service.TelegramMessageSender;
import com.booking.stadium.telegram.subscription.TelegramSubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TelegramCommandHandlerTest {

    @Mock
    private TelegramSubscriptionService subscriptionService;
    @Mock
    private TelegramConversationService conversationService;
    @Mock
    private TelegramMessageSender messageSender;

    @Test
    void handleSanTrong_shouldMoveToAskDateState() {
        TelegramCommandHandler handler = new TelegramCommandHandler(subscriptionService, conversationService, messageSender);
        TelegramIncomingMessage message = TelegramIncomingMessage.builder()
                .chatId(123L)
                .telegramUserId(999L)
                .text("/san_trong")
                .build();

        boolean handled = handler.handle(message);
        assertTrue(handled);
        verify(conversationService).setState(eq(123L), eq(TelegramConversationState.ASK_DATE), any(TelegramConversationPayload.class));
        verify(messageSender).sendMessage(eq(123L), any(String.class));
    }
}
