package com.booking.stadium.dto.booking;

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
public class AvailableSlotResponse {

    private Long timeSlotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private Boolean isAvailable;
}
