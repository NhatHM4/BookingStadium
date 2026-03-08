package com.booking.stadium.entity;

import com.booking.stadium.enums.RecurrenceType;
import com.booking.stadium.enums.RecurringBookingStatus;
import com.booking.stadium.enums.RecurringDepositStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recurring_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recurring_code", unique = true, length = 20)
    private String recurringCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false)
    private RecurrenceType recurrenceType;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_sessions")
    private Integer totalSessions;

    @Column(name = "completed_sessions")
    @Builder.Default
    private Integer completedSessions = 0;

    @Column(name = "cancelled_sessions")
    @Builder.Default
    private Integer cancelledSessions = 0;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "original_price_per_session", precision = 10, scale = 2)
    private BigDecimal originalPricePerSession;

    @Column(name = "discounted_price_per_session", precision = 10, scale = 2)
    private BigDecimal discountedPricePerSession;

    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "total_deposit", precision = 12, scale = 2)
    private BigDecimal totalDeposit;

    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_status")
    @Builder.Default
    private RecurringDepositStatus depositStatus = RecurringDepositStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RecurringBookingStatus status = RecurringBookingStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "recurringBooking", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();
}
