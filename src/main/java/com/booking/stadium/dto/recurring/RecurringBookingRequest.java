package com.booking.stadium.dto.recurring;

import com.booking.stadium.enums.RecurrenceType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringBookingRequest {

    @NotNull(message = "Field ID không được để trống")
    private Long fieldId;

    @NotNull(message = "TimeSlot ID không được để trống")
    private Long timeSlotId;

    @NotNull(message = "Kiểu lặp không được để trống")
    private RecurrenceType recurrenceType;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    private String note;
}
