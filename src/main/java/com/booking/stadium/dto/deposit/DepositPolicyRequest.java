package com.booking.stadium.dto.deposit;

import com.booking.stadium.entity.DepositPolicy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositPolicyRequest {

    @NotNull(message = "Phần trăm cọc không được để trống")
    @Min(value = 0, message = "Phần trăm cọc phải >= 0")
    @Max(value = 100, message = "Phần trăm cọc phải <= 100")
    private BigDecimal depositPercent;

    private Integer refundBeforeHours = 24;

    @Min(value = 0) @Max(value = 100)
    private BigDecimal refundPercent = new BigDecimal("100");

    @Min(value = 0) @Max(value = 100)
    private BigDecimal lateCancelRefundPercent = BigDecimal.ZERO;

    @Min(value = 0) @Max(value = 100)
    private BigDecimal recurringDiscountPercent = BigDecimal.ZERO;

    private Integer minRecurringSessions = 4;

    private Boolean isDepositRequired = true;
}
