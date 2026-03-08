package com.booking.stadium.dto.match;

import com.booking.stadium.entity.MatchRequest;
import com.booking.stadium.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRequestResponse {

    private Long id;
    private String matchCode;

    // Booking info
    private Long bookingId;
    private String bookingCode;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Stadium/Field info
    private Long stadiumId;
    private String stadiumName;
    private String stadiumAddress;
    private Long fieldId;
    private String fieldName;

    // Host team info
    private Long hostTeamId;
    private String hostTeamName;
    private String hostTeamLogoUrl;

    // Opponent team info
    private Long opponentTeamId;
    private String opponentTeamName;
    private String opponentTeamLogoUrl;

    // Match settings
    private FieldType fieldType;
    private SkillLevel requiredSkillLevel;
    private CostSharing costSharing;
    private BigDecimal hostSharePercent;
    private BigDecimal opponentSharePercent;
    private BigDecimal totalPrice;
    private BigDecimal opponentAmount;

    private String message;
    private String contactPhone;
    private MatchStatus status;
    private LocalDateTime acceptedAt;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;

    private Integer responseCount;
    private List<MatchResponseResponse> responses;

    public static MatchRequestResponse fromEntity(MatchRequest mr) {
        MatchRequestResponseBuilder builder = MatchRequestResponse.builder()
                .id(mr.getId())
                .matchCode(mr.getMatchCode())
                .bookingId(mr.getBooking().getId())
                .bookingCode(mr.getBooking().getBookingCode())
                .bookingDate(mr.getBooking().getBookingDate())
                .startTime(mr.getBooking().getTimeSlot().getStartTime())
                .endTime(mr.getBooking().getTimeSlot().getEndTime())
                .stadiumId(mr.getBooking().getField().getStadium().getId())
                .stadiumName(mr.getBooking().getField().getStadium().getName())
                .stadiumAddress(mr.getBooking().getField().getStadium().getAddress())
                .fieldId(mr.getBooking().getField().getId())
                .fieldName(mr.getBooking().getField().getName())
                .hostTeamId(mr.getHostTeam().getId())
                .hostTeamName(mr.getHostTeam().getName())
                .hostTeamLogoUrl(mr.getHostTeam().getLogoUrl())
                .fieldType(mr.getFieldType())
                .requiredSkillLevel(mr.getRequiredSkillLevel())
                .costSharing(mr.getCostSharing())
                .hostSharePercent(mr.getHostSharePercent())
                .opponentSharePercent(mr.getOpponentSharePercent())
                .totalPrice(mr.getBooking().getTotalPrice())
                .opponentAmount(mr.getOpponentAmount())
                .message(mr.getMessage())
                .contactPhone(mr.getContactPhone())
                .status(mr.getStatus())
                .acceptedAt(mr.getAcceptedAt())
                .expiredAt(mr.getExpiredAt())
                .createdAt(mr.getCreatedAt());

        if (mr.getOpponentTeam() != null) {
            builder.opponentTeamId(mr.getOpponentTeam().getId())
                    .opponentTeamName(mr.getOpponentTeam().getName())
                    .opponentTeamLogoUrl(mr.getOpponentTeam().getLogoUrl());
        }

        return builder.build();
    }

    public static MatchRequestResponse fromEntityWithResponses(MatchRequest mr, List<MatchResponseResponse> responses) {
        MatchRequestResponse response = fromEntity(mr);
        response.setResponses(responses);
        response.setResponseCount(responses != null ? responses.size() : 0);
        return response;
    }
}
