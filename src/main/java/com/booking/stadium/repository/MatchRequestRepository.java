package com.booking.stadium.repository;

import com.booking.stadium.entity.MatchRequest;
import com.booking.stadium.enums.FieldType;
import com.booking.stadium.enums.MatchStatus;
import com.booking.stadium.enums.SkillLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    Optional<MatchRequest> findByMatchCode(String matchCode);

    Optional<MatchRequest> findByBookingId(Long bookingId);

    List<MatchRequest> findByHostTeamId(Long teamId);

    Page<MatchRequest> findByStatus(MatchStatus status, Pageable pageable);

    @Query("SELECT mr FROM MatchRequest mr WHERE mr.status = 'OPEN' " +
            "AND (:fieldType IS NULL OR mr.fieldType = :fieldType) " +
            "AND (:skillLevel IS NULL OR mr.requiredSkillLevel = :skillLevel OR mr.requiredSkillLevel = 'ANY') " +
            "AND (:excludeUserId IS NULL OR mr.hostTeam.id NOT IN (SELECT t.id FROM Team t JOIN t.members tm WHERE tm.user.id = :excludeUserId AND tm.status = 'ACTIVE'))")
    Page<MatchRequest> searchOpenMatches(
            @Param("fieldType") FieldType fieldType,
            @Param("skillLevel") SkillLevel skillLevel,
            @Param("excludeUserId") Long excludeUserId,
            Pageable pageable);

    @Query("SELECT mr FROM MatchRequest mr WHERE mr.status = 'OPEN' " +
            "AND mr.expiredAt < CURRENT_TIMESTAMP")
    List<MatchRequest> findExpiredOpenMatches();
}
