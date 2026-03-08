package com.booking.stadium.dto.match;

import com.booking.stadium.enums.CostSharing;
import com.booking.stadium.enums.SkillLevel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequestRequest {

    @NotNull(message = "Booking ID không được để trống")
    private Long bookingId;

    @NotNull(message = "Team ID không được để trống")
    private Long teamId;

    private SkillLevel requiredSkillLevel;

    private CostSharing costSharing;

    private BigDecimal hostSharePercent;

    private BigDecimal opponentSharePercent;

    private String message;

    private String contactPhone;
}
