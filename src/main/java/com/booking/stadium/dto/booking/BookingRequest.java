package com.booking.stadium.dto.booking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Field ID không được để trống")
    private Long fieldId;

    @NotNull(message = "Time Slot ID không được để trống")
    private Long timeSlotId;

    @NotNull(message = "Ngày đặt sân không được để trống")
    private LocalDate bookingDate;

    private String note;

    private Boolean isMatchRequest = false;
}
