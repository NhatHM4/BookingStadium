package com.booking.stadium.telegram.service;

public class TelegramRateLimitException extends TelegramSendException {

    private final Integer retryAfterSeconds;

    public TelegramRateLimitException(String message, Integer retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
