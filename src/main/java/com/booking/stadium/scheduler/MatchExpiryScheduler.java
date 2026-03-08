package com.booking.stadium.scheduler;

import com.booking.stadium.entity.MatchRequest;
import com.booking.stadium.entity.MatchResponse;
import com.booking.stadium.enums.MatchResponseStatus;
import com.booking.stadium.enums.MatchStatus;
import com.booking.stadium.repository.MatchRequestRepository;
import com.booking.stadium.repository.MatchResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Scheduler tự động expire kèo ráp khi đã quá hạn (trước giờ đá 2 tiếng)
 * Chạy mỗi 5 phút
 */
@Component
public class MatchExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(MatchExpiryScheduler.class);

    private final MatchRequestRepository matchRequestRepository;
    private final MatchResponseRepository matchResponseRepository;

    public MatchExpiryScheduler(MatchRequestRepository matchRequestRepository,
                                MatchResponseRepository matchResponseRepository) {
        this.matchRequestRepository = matchRequestRepository;
        this.matchResponseRepository = matchResponseRepository;
    }

    @Scheduled(fixedRate = 300000) // 5 phút
    @Transactional
    public void expireOpenMatches() {
        List<MatchRequest> expiredMatches = matchRequestRepository.findExpiredOpenMatches();

        if (expiredMatches.isEmpty()) return;

        for (MatchRequest mr : expiredMatches) {
            mr.setStatus(MatchStatus.EXPIRED);

            // Từ chối tất cả responses đang pending
            List<MatchResponse> pendingResponses = matchResponseRepository
                    .findByMatchRequestIdAndStatus(mr.getId(), MatchResponseStatus.PENDING);
            for (MatchResponse resp : pendingResponses) {
                resp.setStatus(MatchResponseStatus.REJECTED);
            }
            matchResponseRepository.saveAll(pendingResponses);

            log.info("Auto-expired match request: {} (matchCode={})", mr.getId(), mr.getMatchCode());
        }

        matchRequestRepository.saveAll(expiredMatches);
        log.info("Auto-expired {} match requests", expiredMatches.size());
    }
}
