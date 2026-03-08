package com.booking.stadium.entity;

import com.booking.stadium.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "match_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_code", unique = true, length = 20)
    private String matchCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_team_id", nullable = false)
    private Team hostTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_team_id")
    private Team opponentTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false)
    private FieldType fieldType;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_skill_level")
    @Builder.Default
    private SkillLevel requiredSkillLevel = SkillLevel.ANY;

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_sharing")
    @Builder.Default
    private CostSharing costSharing = CostSharing.EQUAL_SPLIT;

    @Column(name = "host_share_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal hostSharePercent = new BigDecimal("50.00");

    @Column(name = "opponent_share_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal opponentSharePercent = new BigDecimal("50.00");

    @Column(name = "opponent_amount", precision = 10, scale = 2)
    private BigDecimal opponentAmount;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MatchStatus status = MatchStatus.OPEN;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "matchRequest", cascade = CascadeType.ALL)
    @Builder.Default
    private List<MatchResponse> responses = new ArrayList<>();
}
