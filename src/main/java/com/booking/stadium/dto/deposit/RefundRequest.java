package com.booking.stadium.dto.deposit;

import com.booking.stadium.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    private PaymentMethod paymentMethod;

    private String note;
}
