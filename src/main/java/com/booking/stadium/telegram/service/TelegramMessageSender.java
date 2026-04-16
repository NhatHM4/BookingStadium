package com.booking.stadium.telegram.service;

public interface TelegramMessageSender {

    void sendMessage(Long chatId, String text);
}
