package com.booking.stadium.telegram.service;

public final class TelegramLogMasker {

    private TelegramLogMasker() {
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "***";
        }
        int keep = Math.min(3, phone.length() - 1);
        return "*".repeat(phone.length() - keep) + phone.substring(phone.length() - keep);
    }
}
