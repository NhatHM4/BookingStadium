package com.booking.stadium.entity;

import com.booking.stadium.enums.FieldType;
import com.booking.stadium.enums.SkillLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_field_type")
    private FieldType preferredFieldType;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level")
    @Builder.Default
    private SkillLevel skillLevel = SkillLevel.INTERMEDIATE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captain_id", nullable = false)
    private User captain;

    @Column(name = "member_count")
    @Builder.Default
    private Integer memberCount = 1;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TeamMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "hostTeam", cascade = CascadeType.ALL)
    @Builder.Default
    private List<MatchRequest> hostedMatches = new ArrayList<>();
}
