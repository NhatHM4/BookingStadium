package com.booking.stadium.dto.timeslot;

import com.booking.stadium.entity.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponse {

    private Long id;
    private Long fieldId;
    private String fieldName;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private Boolean isActive;

    public static TimeSlotResponse fromEntity(TimeSlot timeSlot) {
        return TimeSlotResponse.builder()
                .id(timeSlot.getId())
                .fieldId(timeSlot.getField().getId())
                .fieldName(timeSlot.getField().getName())
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .price(timeSlot.getPrice())
                .isActive(timeSlot.getIsActive())
                .build();
    }
}
