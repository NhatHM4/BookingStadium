package com.booking.stadium.telegram.handlers;

import com.booking.stadium.telegram.conversation.*;
import com.booking.stadium.telegram.dto.TelegramIncomingMessage;
import com.booking.stadium.telegram.enums.TelegramConversationState;
import com.booking.stadium.telegram.lead.TelegramBookingLeadService;
import com.booking.stadium.telegram.service.TelegramAvailabilityService;
import com.booking.stadium.telegram.service.TelegramInputValidator;
import com.booking.stadium.telegram.service.TelegramMessageSender;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class TelegramConversationHandler {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final TelegramConversationService conversationService;
    private final TelegramInputValidator inputValidator;
    private final TelegramAvailabilityService availabilityService;
    private final TelegramBookingLeadService bookingLeadService;
    private final TelegramMessageSender messageSender;

    public TelegramConversationHandler(TelegramConversationService conversationService,
                                       TelegramInputValidator inputValidator,
                                       TelegramAvailabilityService availabilityService,
                                       TelegramBookingLeadService bookingLeadService,
                                       TelegramMessageSender messageSender) {
        this.conversationService = conversationService;
        this.inputValidator = inputValidator;
        this.availabilityService = availabilityService;
        this.bookingLeadService = bookingLeadService;
        this.messageSender = messageSender;
    }

    public boolean handle(TelegramIncomingMessage incomingMessage, TelegramConversation conversation) {
        if (conversation.getState() == TelegramConversationState.IDLE) {
            return false;
        }

        TelegramConversationPayload payload = conversationService.getPayload(conversation);
        String text = incomingMessage.getText();

        return switch (conversation.getState()) {
            case ASK_DATE -> handleAskDate(incomingMessage, payload, text);
            case ASK_TIME -> handleAskTime(incomingMessage, payload, text);
            case ASK_LOCATION -> handleAskLocation(incomingMessage, payload, text);
            case SHOW_SLOTS -> handleShowSlots(incomingMessage, payload, text);
            case ASK_PHONE -> handleAskPhone(incomingMessage, payload, text);
            case ASK_NAME -> handleAskName(incomingMessage, payload, text);
            case CONFIRM -> handleConfirm(incomingMessage, payload, text);
            default -> false;
        };
    }

    private boolean handleAskDate(TelegramIncomingMessage incomingMessage, TelegramConversationPayload payload, String text) {
        try {
            payload.setBookingDate(inputValidator.parseDate(text));
            conversationService.setState(incomingMessage.getChatId(), TelegramConversationState.ASK_TIME, payload);
            messageSender.sendMessage(incomingMessage.getChatId(), "Nhập giờ mong muốn (HH:mm):");
        } catch (IllegalArgumentException ex) {
            messageSender.sendMessage(incomingMessage.getChatId(), ex.getMessage() + "\nNhập lại ngày (YYYY-MM-DD):");
        }
        return true;
    }

    private boolean handleAskTime(TelegramIncomingMessage incomingMessage, TelegramConversationPayload payload, String text) {
        try {
            payload.setBookingTime(inputValidator.parseTime(text, payload.getBookingDate()));
            conversationService.setState(incomingMessage.getChatId(), TelegramConversationState.ASK_LOCATION, payload);
            messageSender.sendMessage(incomingMessage.getChatId(),
                    "Nhập quận/huyện ưu tiên (hoặc gõ 'bo qua' nếu không):");
        } catch (IllegalArgumentException ex) {
            messageSender.sendMessage(incomingMessage.getChatId(), ex.getMessage() + "\nNhập lại giờ (HH:mm):");
        }
        return true;
    }

    private boolean handleAskLocation(TelegramIncomingMessage incomingMessage, TelegramConversationPayload payload, String text) {
        String district = normalizeDistrict(text);
        payload.setDistrict(district);

        List<TelegramSlotOption> slots = availabilityService.search(payload.getBookingDate(), payload.getBookingTime(), district)
                .stream()
                .limit(5)
                .toList();
        if (slots.isEmpty()) {
            List<TelegramSlotOption> nearby = availabilityService.searchNearby(payload.getBookingDate(), payload.getBookingTime(), district)
                    .stream()
                    .limit(3)
                    .toList();
            if (nearby.isEmpty()) {
                messageSender.sendMessage(incomingMessage.getChatId(),
                        "Hiện chưa có sân phù hợp. Bạn thử lại bằng giờ khác hoặc quận khác nhé.");
            } else {
                messageSender.sendMessage(incomingMessage.getChatId(),
                        "Chưa có slot đúng tiêu chí. Gợi ý gần nhất:\n" + formatSlotOptions(nearby));
            }
            conversationService.setState(incomingMessage.getChatId(), TelegramConversationState.ASK_TIME, payload);
            messageSender.sendMessage(incomingMessage.getChatId(), "Nhập lại giờ mong muốn (HH:mm):");
            return true;
        }

        payload.setSlotOptions(slots);
        conversationService.setState(incomingMessage.getChatId(), TelegramConversationState.SHOW_SLOTS, payload);
        messageSender.sendMessage(incomingMessage.getChatId(),
                "Mình tìm thấy các slot sau, trả lời bằng số thứ tự:\n" + formatSlotOptions(slots));
        return true;
    }

    private boolean handleShowSlots(TelegramIncomingMessage incomingMessage, TelegramConversationPayload payload, String text) {
        try {
            int selectedIndex = Integer.parseInt(text.trim());
            if (selectedIndex < 1 || selectedIndex > payload.getSlotOptions().size()) {
                throw new IllegalArgumentException("Chỉ số vượt quá danh sách");
            }
            payload.setSelectedSlot(payload.getSlotOptions().get(selectedIndex - 1));
            conversationService.setState(incomingMessage.getChatId(), TelegramConversationState.ASK_PHONE, payload);
            messageSender.sendMessage(incomingMessage.getChatId(), "Nhập số điện thoại liên hệ:");
        } catch (Exception ex) {
            messageSender.sendMessage(incomingMessage.getChatId(), "Vui lòng nhập số thứ tự hợp lệ (1.." + payload.getSlotOptions().size() + ").");
        }
        return true;
    }

    private boolean handleAskPhone(TelegramIncomingMessage incomingMessage, TelegramConversationPayload payload, String text) {
        try {
            payload.setPhone(inputValidator.normalizePhone(text));
            conversationService.setState(incomingMessage.getChatId(), TelegramConversationState.ASK_NAME, payload);
            messageSender.sendMessage(incomingMessage.getChatId(), "Nhập tên người đặt sân:");
        } catch (IllegalArgumentException ex) {
            messageSender.sendMessage(incomingMessage.getChatId(), ex.getMessage() + "\nNhập lại số điện thoại:");
        }
        return true;
    }

    private boolean handleAskName(TelegramIncomingMessage incomingMessage, TelegramConversationPayload payload, String text) {
        if (text == null || text.isBlank()) {
            messageSender.sendMessage(incomingMessage.getChatId(), "Tên người đặt không được để trống. Vui lòng nhập lại:");
            return true;
        }
        payload.setBookerName(text.trim());
        conversationService.setState(incomingMessage.getChatId(), TelegramConversationState.CONFIRM, payload);
        messageSender.sendMessage(incomingMessage.getChatId(), formatConfirmation(payload));
        return true;
    }

    private boolean handleConfirm(TelegramIncomingMessage incomingMessage, TelegramConversationPayload payload, String text) {
        String answer = text.trim().toLowerCase();
        if (answer.equals("yes") || answer.equals("y") || answer.equals("co")) {
            bookingLeadService.createLead(incomingMessage, payload);
            conversationService.clear(incomingMessage.getChatId());
            messageSender.sendMessage(incomingMessage.getChatId(),
                    "Đã ghi nhận yêu cầu của bạn. Bộ phận CSKH sẽ liên hệ sớm để chốt sân.");
            return true;
        }
        if (answer.equals("no") || answer.equals("n") || answer.equals("khong")) {
            conversationService.clear(incomingMessage.getChatId());
            messageSender.sendMessage(incomingMessage.getChatId(), "Đã hủy yêu cầu. Bạn có thể bắt đầu lại bằng /san_trong.");
            return true;
        }
        messageSender.sendMessage(incomingMessage.getChatId(), "Vui lòng trả lời 'yes' hoặc 'no'.");
        return true;
    }

    private String normalizeDistrict(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        if (normalized.equalsIgnoreCase("bo qua")) {
            return null;
        }
        return normalized.isBlank() ? null : normalized;
    }

    private String formatSlotOptions(List<TelegramSlotOption> slots) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < slots.size(); i++) {
            TelegramSlotOption option = slots.get(i);
            builder.append(i + 1).append(". ")
                    .append(option.getStadiumName()).append(" - ").append(option.getFieldName())
                    .append(" | ").append(option.getStartTime().format(TIME_FORMAT)).append("-")
                    .append(option.getEndTime().format(TIME_FORMAT))
                    .append(" | ").append(option.getPrice()).append("đ");
            if (i < slots.size() - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private String formatConfirmation(TelegramConversationPayload payload) {
        TelegramSlotOption selected = payload.getSelectedSlot();
        String slotText = selected == null
                ? "Chưa chọn"
                : selected.getStadiumName() + " - " + selected.getFieldName()
                + " (" + selected.getStartTime().format(TIME_FORMAT) + "-" + selected.getEndTime().format(TIME_FORMAT) + ")";
        return """
                Vui lòng xác nhận:
                - Ngày: %s
                - Giờ mong muốn: %s
                - Slot chọn: %s
                - SĐT: %s
                - Người đặt: %s

                Trả lời 'yes' để xác nhận, 'no' để hủy.
                """.formatted(
                payload.getBookingDate(),
                payload.getBookingTime().format(TIME_FORMAT),
                slotText,
                payload.getPhone(),
                payload.getBookerName());
    }
}
