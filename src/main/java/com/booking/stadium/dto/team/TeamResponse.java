package com.booking.stadium.dto.team;

import com.booking.stadium.entity.Team;
import com.booking.stadium.enums.FieldType;
import com.booking.stadium.enums.SkillLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {

    private Long id;
    private String name;
    private String logoUrl;
    private String description;
    private FieldType preferredFieldType;
    private SkillLevel skillLevel;
    private Long captainId;
    private String captainName;
    private Integer memberCount;
    private String city;
    private String district;
    private Boolean isActive;
    private LocalDateTime createdAt;

    private List<TeamMemberResponse> members;

    public static TeamResponse fromEntity(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .logoUrl(team.getLogoUrl())
                .description(team.getDescription())
                .preferredFieldType(team.getPreferredFieldType())
                .skillLevel(team.getSkillLevel())
                .captainId(team.getCaptain().getId())
                .captainName(team.getCaptain().getFullName())
                .memberCount(team.getMemberCount())
                .city(team.getCity())
                .district(team.getDistrict())
                .isActive(team.getIsActive())
                .createdAt(team.getCreatedAt())
                .build();
    }

    public static TeamResponse fromEntityWithMembers(Team team, List<TeamMemberResponse> members) {
        TeamResponse response = fromEntity(team);
        response.setMembers(members);
        return response;
    }
}
