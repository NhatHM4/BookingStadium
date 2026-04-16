package com.booking.stadium.telegram.lead;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "telegram_booking_leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramBookingLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "telegram_user_id")
    private Long telegramUserId;

    @Column(name = "telegram_username", length = 100)
    private String telegramUsername;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "requested_time", nullable = false)
    private LocalTime requestedTime;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "field_id")
    private Long fieldId;

    @Column(name = "time_slot_id")
    private Long timeSlotId;

    @Column(name = "stadium_name", length = 255)
    private String stadiumName;

    @Column(name = "field_name", length = 255)
    private String fieldName;

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Column(name = "booker_name", length = 150, nullable = false)
    private String bookerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TelegramBookingLeadStatus status = TelegramBookingLeadStatus.NEW;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
