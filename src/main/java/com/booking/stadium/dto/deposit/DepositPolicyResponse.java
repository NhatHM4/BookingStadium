package com.booking.stadium.dto.deposit;

import com.booking.stadium.entity.DepositPolicy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositPolicyResponse {

    private Long id;
    private Long stadiumId;
    private String stadiumName;
    private BigDecimal depositPercent;
    private Integer refundBeforeHours;
    private BigDecimal refundPercent;
    private BigDecimal lateCancelRefundPercent;
    private BigDecimal recurringDiscountPercent;
    private Integer minRecurringSessions;
    private Boolean isDepositRequired;

    public static DepositPolicyResponse fromEntity(DepositPolicy policy) {
        return DepositPolicyResponse.builder()
                .id(policy.getId())
                .stadiumId(policy.getStadium().getId())
                .stadiumName(policy.getStadium().getName())
                .depositPercent(policy.getDepositPercent())
                .refundBeforeHours(policy.getRefundBeforeHours())
                .refundPercent(policy.getRefundPercent())
                .lateCancelRefundPercent(policy.getLateCancelRefundPercent())
                .recurringDiscountPercent(policy.getRecurringDiscountPercent())
                .minRecurringSessions(policy.getMinRecurringSessions())
                .isDepositRequired(policy.getIsDepositRequired())
                .build();
    }
}
