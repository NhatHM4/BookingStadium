package com.booking.stadium.dto.match;

import com.booking.stadium.enums.MatchJoinType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseRequest {

    private Long teamId;

    private MatchJoinType joinType;

    private String contactPhone;

    private String message;
}
