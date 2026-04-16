package com.booking.stadium.telegram.conversation;

import com.booking.stadium.telegram.config.TelegramProperties;
import com.booking.stadium.telegram.enums.TelegramConversationState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TelegramConversationService {

    private final TelegramConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;
    private final TelegramProperties properties;

    public TelegramConversationService(TelegramConversationRepository conversationRepository,
                                       ObjectMapper objectMapper,
                                       TelegramProperties properties) {
        this.conversationRepository = conversationRepository;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Transactional
    public TelegramConversation getOrCreate(Long chatId) {
        TelegramConversation conversation = conversationRepository.findByChatId(chatId)
                .orElse(TelegramConversation.builder()
                        .chatId(chatId)
                        .state(TelegramConversationState.IDLE)
                        .payloadJson("{}")
                        .build());

        if (conversation.getExpiresAt() != null && conversation.getExpiresAt().isBefore(LocalDateTime.now())) {
            conversation.setState(TelegramConversationState.IDLE);
            conversation.setPayloadJson("{}");
        }
        return conversationRepository.save(conversation);
    }

    @Transactional
    public void setState(Long chatId, TelegramConversationState state, TelegramConversationPayload payload) {
        TelegramConversation conversation = getOrCreate(chatId);
        conversation.setState(state);
        conversation.setPayloadJson(toJson(payload));
        conversation.setExpiresAt(LocalDateTime.now().plusMinutes(properties.getConversationTimeoutMinutes()));
        conversationRepository.save(conversation);
    }

    @Transactional
    public void clear(Long chatId) {
        TelegramConversation conversation = getOrCreate(chatId);
        conversation.setState(TelegramConversationState.IDLE);
        conversation.setPayloadJson("{}");
        conversation.setExpiresAt(null);
        conversationRepository.save(conversation);
    }

    public TelegramConversationPayload getPayload(TelegramConversation conversation) {
        if (conversation.getPayloadJson() == null || conversation.getPayloadJson().isBlank()) {
            return TelegramConversationPayload.builder().build();
        }
        try {
            return objectMapper.readValue(conversation.getPayloadJson(), TelegramConversationPayload.class);
        } catch (JsonProcessingException e) {
            return TelegramConversationPayload.builder().build();
        }
    }

    private String toJson(TelegramConversationPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? TelegramConversationPayload.builder().build() : payload);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
