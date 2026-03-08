package com.booking.stadium.dto.deposit;

import com.booking.stadium.entity.Deposit;
import com.booking.stadium.enums.DepositTransactionStatus;
import com.booking.stadium.enums.DepositType;
import com.booking.stadium.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponse {

    private Long id;
    private Long bookingId;
    private String bookingCode;
    private BigDecimal amount;
    private DepositType depositType;
    private PaymentMethod paymentMethod;
    private String transactionCode;
    private String note;
    private Long confirmedById;
    private String confirmedByName;
    private LocalDateTime confirmedAt;
    private DepositTransactionStatus status;
    private LocalDateTime createdAt;

    public static DepositResponse fromEntity(Deposit deposit) {
        return DepositResponse.builder()
                .id(deposit.getId())
                .bookingId(deposit.getBooking().getId())
                .bookingCode(deposit.getBooking().getBookingCode())
                .amount(deposit.getAmount())
                .depositType(deposit.getDepositType())
                .paymentMethod(deposit.getPaymentMethod())
                .transactionCode(deposit.getTransactionCode())
                .note(deposit.getNote())
                .confirmedById(deposit.getConfirmedBy() != null ? deposit.getConfirmedBy().getId() : null)
                .confirmedByName(deposit.getConfirmedBy() != null ? deposit.getConfirmedBy().getFullName() : null)
                .confirmedAt(deposit.getConfirmedAt())
                .status(deposit.getStatus())
                .createdAt(deposit.getCreatedAt())
                .build();
    }
}
