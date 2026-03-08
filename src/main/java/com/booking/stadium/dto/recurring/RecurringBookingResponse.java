package com.booking.stadium.dto.recurring;

import com.booking.stadium.dto.booking.BookingResponse;
import com.booking.stadium.entity.RecurringBooking;
import com.booking.stadium.enums.RecurrenceType;
import com.booking.stadium.enums.RecurringBookingStatus;
import com.booking.stadium.enums.RecurringDepositStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringBookingResponse {

    private Long id;
    private String recurringCode;
    private Long customerId;
    private String customerName;
    private Long fieldId;
    private String fieldName;
    private Long stadiumId;
    private String stadiumName;
    private Long timeSlotId;
    private String timeSlotRange;
    private RecurrenceType recurrenceType;
    private Integer dayOfWeek;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalSessions;
    private Integer completedSessions;
    private Integer cancelledSessions;
    private BigDecimal discountPercent;
    private BigDecimal originalPricePerSession;
    private BigDecimal discountedPricePerSession;
    private BigDecimal totalPrice;
    private BigDecimal totalDeposit;
    private RecurringDepositStatus depositStatus;
    private RecurringBookingStatus status;
    private String note;
    private LocalDateTime createdAt;
    private List<BookingResponse> bookings;

    public static RecurringBookingResponse fromEntity(RecurringBooking rb) {
        return fromEntity(rb, null);
    }

    public static RecurringBookingResponse fromEntity(RecurringBooking rb, List<BookingResponse> bookings) {
        return RecurringBookingResponse.builder()
                .id(rb.getId())
                .recurringCode(rb.getRecurringCode())
                .customerId(rb.getCustomer().getId())
                .customerName(rb.getCustomer().getFullName())
                .fieldId(rb.getField().getId())
                .fieldName(rb.getField().getName())
                .stadiumId(rb.getField().getStadium().getId())
                .stadiumName(rb.getField().getStadium().getName())
                .timeSlotId(rb.getTimeSlot().getId())
                .timeSlotRange(rb.getTimeSlot().getStartTime() + " - " + rb.getTimeSlot().getEndTime())
                .recurrenceType(rb.getRecurrenceType())
                .dayOfWeek(rb.getDayOfWeek())
                .startDate(rb.getStartDate())
                .endDate(rb.getEndDate())
                .totalSessions(rb.getTotalSessions())
                .completedSessions(rb.getCompletedSessions())
                .cancelledSessions(rb.getCancelledSessions())
                .discountPercent(rb.getDiscountPercent())
                .originalPricePerSession(rb.getOriginalPricePerSession())
                .discountedPricePerSession(rb.getDiscountedPricePerSession())
                .totalPrice(rb.getTotalPrice())
                .totalDeposit(rb.getTotalDeposit())
                .depositStatus(rb.getDepositStatus())
                .status(rb.getStatus())
                .note(rb.getNote())
                .createdAt(rb.getCreatedAt())
                .bookings(bookings)
                .build();
    }
}
