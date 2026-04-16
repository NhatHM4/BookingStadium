package com.booking.stadium.telegram.event;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;

@Value
@Builder
public class MatchRequestCreatedEvent {
    String eventId;
    Long matchRequestId;
    String matchCode;
    String stadiumName;
    String fieldName;
    LocalDate bookingDate;
    LocalTime startTime;
    LocalTime endTime;
    String hostTeamName;
    String contactPhone;
}
