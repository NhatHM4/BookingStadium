package com.booking.stadium.telegram.handlers;

import com.booking.stadium.telegram.conversation.TelegramConversationPayload;
import com.booking.stadium.telegram.conversation.TelegramConversationService;
import com.booking.stadium.telegram.dto.TelegramIncomingMessage;
import com.booking.stadium.telegram.enums.TelegramConversationState;
import com.booking.stadium.telegram.service.TelegramMessageSender;
import com.booking.stadium.telegram.subscription.TelegramSubscriptionService;
import org.springframework.stereotype.Component;

@Component
public class TelegramCommandHandler {

    private final TelegramSubscriptionService subscriptionService;
    private final TelegramConversationService conversationService;
    private final TelegramMessageSender messageSender;

    public TelegramCommandHandler(TelegramSubscriptionService subscriptionService,
                                  TelegramConversationService conversationService,
                                  TelegramMessageSender messageSender) {
        this.subscriptionService = subscriptionService;
        this.conversationService = conversationService;
        this.messageSender = messageSender;
    }

    public boolean handle(TelegramIncomingMessage incomingMessage) {
        String text = incomingMessage.getText();
        if (!text.startsWith("/")) {
            return false;
        }
        switch (text) {
            case "/start" -> {
                messageSender.sendMessage(incomingMessage.getChatId(), """
                        Chào bạn, mình có thể:
                        - Báo kèo mới: /subscribe_keo hoặc /unsubscribe_keo
                        - Tìm sân trống: /san_trong
                        - Hủy thao tác: /cancel

                        Quyền riêng tư: bot chỉ lưu chat id, trạng thái hội thoại tạm và thông tin bạn tự nhập để hỗ trợ đặt sân.
                        """);
                return true;
            }
            case "/subscribe_keo" -> {
                subscriptionService.subscribeMatchAlert(incomingMessage.getChatId(), incomingMessage.getTelegramUserId());
                messageSender.sendMessage(incomingMessage.getChatId(), "Đã bật nhận thông báo kèo mới.");
                return true;
            }
            case "/unsubscribe_keo" -> {
                subscriptionService.unsubscribeMatchAlert(incomingMessage.getChatId());
                messageSender.sendMessage(incomingMessage.getChatId(), "Đã tắt nhận thông báo kèo mới.");
                return true;
            }
            case "/san_trong" -> {
                conversationService.setState(
                        incomingMessage.getChatId(),
                        TelegramConversationState.ASK_DATE,
                        TelegramConversationPayload.builder().build());
                messageSender.sendMessage(incomingMessage.getChatId(), "Nhập ngày bạn muốn đặt sân (YYYY-MM-DD):");
                return true;
            }
            case "/cancel" -> {
                conversationService.clear(incomingMessage.getChatId());
                messageSender.sendMessage(incomingMessage.getChatId(), "Đã hủy hội thoại hiện tại.");
                return true;
            }
            default -> {
                messageSender.sendMessage(incomingMessage.getChatId(), "Lệnh chưa được hỗ trợ. Dùng /start để xem danh sách lệnh.");
                return true;
            }
        }
    }
}
