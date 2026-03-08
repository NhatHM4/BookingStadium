package com.booking.stadium.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposit_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id", nullable = false, unique = true)
    private Stadium stadium;

    @Column(name = "deposit_percent", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal depositPercent = new BigDecimal("30.00");

    @Column(name = "refund_before_hours")
    @Builder.Default
    private Integer refundBeforeHours = 24;

    @Column(name = "refund_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal refundPercent = new BigDecimal("100.00");

    @Column(name = "late_cancel_refund_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal lateCancelRefundPercent = BigDecimal.ZERO;

    @Column(name = "recurring_discount_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal recurringDiscountPercent = BigDecimal.ZERO;

    @Column(name = "min_recurring_sessions")
    @Builder.Default
    private Integer minRecurringSessions = 4;

    @Column(name = "is_deposit_required")
    @Builder.Default
    private Boolean isDepositRequired = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
