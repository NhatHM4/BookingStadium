package com.booking.stadium.telegram.lead;

import com.booking.stadium.telegram.conversation.TelegramConversationPayload;
import com.booking.stadium.telegram.conversation.TelegramSlotOption;
import com.booking.stadium.telegram.dto.TelegramIncomingMessage;
import com.booking.stadium.telegram.service.TelegramLogMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TelegramBookingLeadService {

    private final TelegramBookingLeadRepository bookingLeadRepository;

    public TelegramBookingLeadService(TelegramBookingLeadRepository bookingLeadRepository) {
        this.bookingLeadRepository = bookingLeadRepository;
    }

    @Transactional
    public TelegramBookingLead createLead(TelegramIncomingMessage incomingMessage, TelegramConversationPayload payload) {
        TelegramSlotOption selected = payload.getSelectedSlot();
        TelegramBookingLead lead = TelegramBookingLead.builder()
                .chatId(incomingMessage.getChatId())
                .telegramUserId(incomingMessage.getTelegramUserId())
                .telegramUsername(incomingMessage.getUsername())
                .bookingDate(payload.getBookingDate())
                .requestedTime(payload.getBookingTime())
                .district(payload.getDistrict())
                .fieldId(selected == null ? null : selected.getFieldId())
                .timeSlotId(selected == null ? null : selected.getSlotId())
                .stadiumName(selected == null ? null : selected.getStadiumName())
                .fieldName(selected == null ? null : selected.getFieldName())
                .phone(payload.getPhone())
                .bookerName(payload.getBookerName())
                .build();
        lead = bookingLeadRepository.save(lead);
        log.info("Created telegram booking lead id={} chatId={} phone={}",
                lead.getId(), lead.getChatId(), TelegramLogMasker.maskPhone(lead.getPhone()));
        return lead;
    }
}
