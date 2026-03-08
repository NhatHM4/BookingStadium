package com.booking.stadium.dto.team;

import com.booking.stadium.entity.TeamMember;
import com.booking.stadium.enums.TeamMemberRole;
import com.booking.stadium.enums.TeamMemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberResponse {

    private Long id;
    private Long teamId;
    private String teamName;
    private Long userId;
    private String userName;
    private String userEmail;
    private TeamMemberRole role;
    private TeamMemberStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;

    public static TeamMemberResponse fromEntity(TeamMember tm) {
        return TeamMemberResponse.builder()
                .id(tm.getId())
                .teamId(tm.getTeam().getId())
                .teamName(tm.getTeam().getName())
                .userId(tm.getUser().getId())
                .userName(tm.getUser().getFullName())
                .userEmail(tm.getUser().getEmail())
                .role(tm.getRole())
                .status(tm.getStatus())
                .joinedAt(tm.getJoinedAt())
                .createdAt(tm.getCreatedAt())
                .build();
    }
}
