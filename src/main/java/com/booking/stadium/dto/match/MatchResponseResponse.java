package com.booking.stadium.dto.match;

import com.booking.stadium.entity.MatchResponse;
import com.booking.stadium.enums.MatchResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponseResponse {

    private Long id;
    private Long matchRequestId;
    private Long teamId;
    private String teamName;
    private String teamLogoUrl;
    private String message;
    private MatchResponseStatus status;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;

    public static MatchResponseResponse fromEntity(MatchResponse mr) {
        return MatchResponseResponse.builder()
                .id(mr.getId())
                .matchRequestId(mr.getMatchRequest().getId())
                .teamId(mr.getTeam().getId())
                .teamName(mr.getTeam().getName())
                .teamLogoUrl(mr.getTeam().getLogoUrl())
                .message(mr.getMessage())
                .status(mr.getStatus())
                .respondedAt(mr.getRespondedAt())
                .createdAt(mr.getCreatedAt())
                .build();
    }
}
