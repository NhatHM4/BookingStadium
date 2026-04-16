package com.booking.stadium.telegram.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@Component
public class TelegramInputValidator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final Pattern E164_PHONE = Pattern.compile("^\\+[1-9]\\d{8,14}$");

    public LocalDate parseDate(String input) {
        try {
            LocalDate date = LocalDate.parse(input.trim(), DATE_FORMATTER);
            if (date.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Ngày không được ở quá khứ.");
            }
            return date;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ngày chưa đúng định dạng YYYY-MM-DD.");
        }
    }

    public LocalTime parseTime(String input, LocalDate bookingDate) {
        try {
            LocalTime time = LocalTime.parse(input.trim(), TIME_FORMATTER);
            LocalDateTime dateTime = bookingDate.atTime(time);
            if (dateTime.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Thời điểm bạn chọn đã ở quá khứ.");
            }
            return time;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Giờ chưa đúng định dạng HH:mm.");
        }
    }

    public String normalizePhone(String rawPhone) {
        if (rawPhone == null) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
        }
        String compact = rawPhone.replaceAll("[\\s.-]", "");
        if (compact.startsWith("00")) {
            compact = "+" + compact.substring(2);
        } else if (compact.startsWith("0")) {
            compact = "+84" + compact.substring(1);
        } else if (!compact.startsWith("+")) {
            compact = "+" + compact;
        }

        if (!E164_PHONE.matcher(compact).matches()) {
            throw new IllegalArgumentException("Số điện thoại chưa đúng định dạng quốc tế.");
        }
        return compact;
    }
}
