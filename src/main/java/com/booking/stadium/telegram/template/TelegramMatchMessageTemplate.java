package com.booking.stadium.telegram.template;

import com.booking.stadium.telegram.event.MatchRequestCreatedEvent;
import org.springframework.stereotype.Component;

@Component
public class TelegramMatchMessageTemplate {

    public String renderNewMatch(MatchRequestCreatedEvent event, String appBaseUrl) {
        return """
                Kèo mới
                Mã kèo: %s
                Sân: %s - %s
                Ngày: %s
                Giờ: %s-%s
                Đội chủ: %s
                Liên hệ: %s
                Xem chi tiết: %s/matches/%s
                """.formatted(
                valueOrDash(event.getMatchCode()),
                valueOrDash(event.getStadiumName()),
                valueOrDash(event.getFieldName()),
                valueOrDash(event.getBookingDate() == null ? null : event.getBookingDate().toString()),
                valueOrDash(event.getStartTime() == null ? null : event.getStartTime().toString()),
                valueOrDash(event.getEndTime() == null ? null : event.getEndTime().toString()),
                valueOrDash(event.getHostTeamName()),
                valueOrDash(event.getContactPhone()),
                normalizeBaseUrl(appBaseUrl),
                event.getMatchRequestId() == null ? "-" : event.getMatchRequestId());
    }

    private String normalizeBaseUrl(String appBaseUrl) {
        if (appBaseUrl == null || appBaseUrl.isBlank()) {
            return "http://localhost:3000";
        }
        if (appBaseUrl.endsWith("/")) {
            return appBaseUrl.substring(0, appBaseUrl.length() - 1);
        }
        return appBaseUrl;
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
