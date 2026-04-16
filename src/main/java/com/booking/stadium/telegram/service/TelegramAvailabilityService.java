package com.booking.stadium.telegram.service;

import com.booking.stadium.dto.booking.AvailableSlotResponse;
import com.booking.stadium.entity.Field;
import com.booking.stadium.repository.FieldRepository;
import com.booking.stadium.telegram.conversation.TelegramSlotOption;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class TelegramAvailabilityService {

    private final FieldRepository fieldRepository;
    private final com.booking.stadium.service.BookingService bookingService;

    public TelegramAvailabilityService(FieldRepository fieldRepository, com.booking.stadium.service.BookingService bookingService) {
        this.fieldRepository = fieldRepository;
        this.bookingService = bookingService;
    }

    @Transactional(readOnly = true)
    public List<TelegramSlotOption> search(LocalDate date, LocalTime time, String district) {
        List<Field> fields = fieldRepository.findActiveFieldsWithStadium();
        String districtLower = district == null ? null : district.toLowerCase(Locale.ROOT).trim();

        return fields.stream()
                .filter(field -> districtLower == null || districtLower.isBlank()
                        || (field.getStadium().getDistrict() != null
                        && field.getStadium().getDistrict().toLowerCase(Locale.ROOT).contains(districtLower)))
                .flatMap(field -> bookingService.getAvailableSlots(field.getId(), date).stream()
                        .filter(AvailableSlotResponse::getIsAvailable)
                        .map(slot -> toOption(field, slot)))
                .filter(option -> !option.getStartTime().isBefore(time))
                .sorted(Comparator.comparing(TelegramSlotOption::getStartTime))
                .toList();
    }

    public List<TelegramSlotOption> searchNearby(LocalDate date, LocalTime time, String district) {
        List<TelegramSlotOption> sameDate = search(date, time, district);
        if (!sameDate.isEmpty()) {
            return sameDate;
        }
        return search(date.plusDays(1), time, district);
    }

    private TelegramSlotOption toOption(Field field, AvailableSlotResponse slot) {
        return TelegramSlotOption.builder()
                .slotId(slot.getTimeSlotId())
                .fieldId(field.getId())
                .stadiumName(field.getStadium().getName())
                .fieldName(field.getName())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .price(slot.getPrice())
                .build();
    }
}
