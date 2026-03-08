package com.booking.stadium.repository;

import com.booking.stadium.entity.MatchResponse;
import com.booking.stadium.enums.MatchResponseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchResponseRepository extends JpaRepository<MatchResponse, Long> {

    List<MatchResponse> findByMatchRequestId(Long matchRequestId);

    List<MatchResponse> findByMatchRequestIdAndStatus(Long matchRequestId, MatchResponseStatus status);

    List<MatchResponse> findByTeamId(Long teamId);

    Optional<MatchResponse> findByMatchRequestIdAndTeamId(Long matchRequestId, Long teamId);

    boolean existsByMatchRequestIdAndTeamId(Long matchRequestId, Long teamId);
}
