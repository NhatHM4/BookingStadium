package com.booking.stadium.dto.booking;

import com.booking.stadium.entity.Booking;
import com.booking.stadium.enums.BookingStatus;
import com.booking.stadium.enums.DepositStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private String bookingCode;
    private Long customerId;
    private String customerName;
    private Long fieldId;
    private String fieldName;
    private Long stadiumId;
    private String stadiumName;
    private Long timeSlotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate bookingDate;
    private Boolean isMatchRequest;
    private BigDecimal totalPrice;
    private BigDecimal depositAmount;
    private BigDecimal remainingAmount;
    private DepositStatus depositStatus;
    private String note;
    private BookingStatus status;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private Long recurringBookingId;
    private LocalDateTime createdAt;

    public static BookingResponse fromEntity(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .customerId(booking.getCustomer().getId())
                .customerName(booking.getCustomer().getFullName())
                .fieldId(booking.getField().getId())
                .fieldName(booking.getField().getName())
                .stadiumId(booking.getField().getStadium().getId())
                .stadiumName(booking.getField().getStadium().getName())
                .timeSlotId(booking.getTimeSlot().getId())
                .startTime(booking.getTimeSlot().getStartTime())
                .endTime(booking.getTimeSlot().getEndTime())
                .bookingDate(booking.getBookingDate())
                .isMatchRequest(booking.getIsMatchRequest())
                .totalPrice(booking.getTotalPrice())
                .depositAmount(booking.getDepositAmount())
                .remainingAmount(booking.getRemainingAmount())
                .depositStatus(booking.getDepositStatus())
                .note(booking.getNote())
                .status(booking.getStatus())
                .cancelledAt(booking.getCancelledAt())
                .cancelReason(booking.getCancelReason())
                .recurringBookingId(booking.getRecurringBooking() != null ? booking.getRecurringBooking().getId() : null)
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
