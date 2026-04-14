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

    List<MatchResponse> findByResponderUserId(Long userId);

    Optional<MatchResponse> findByMatchRequestIdAndTeamId(Long matchRequestId, Long teamId);

    Optional<MatchResponse> findByMatchRequestIdAndResponderUserId(Long matchRequestId, Long userId);

    boolean existsByMatchRequestIdAndTeamId(Long matchRequestId, Long teamId);

    boolean existsByMatchRequestIdAndResponderUserId(Long matchRequestId, Long userId);
}
