package com.booking.stadium.service;

import com.booking.stadium.dto.match.*;
import com.booking.stadium.entity.*;
import com.booking.stadium.enums.*;
import com.booking.stadium.exception.BadRequestException;
import com.booking.stadium.exception.ResourceNotFoundException;
import com.booking.stadium.exception.UnauthorizedException;
import com.booking.stadium.repository.*;
import com.booking.stadium.telegram.event.MatchRequestCreatedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class MatchRequestService {

    private final MatchRequestRepository matchRequestRepository;
    private final MatchResponseRepository matchResponseRepository;
    private final BookingRepository bookingRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MatchRequestService(MatchRequestRepository matchRequestRepository,
                               MatchResponseRepository matchResponseRepository,
                               BookingRepository bookingRepository,
                               TeamRepository teamRepository,
                               TeamMemberRepository teamMemberRepository,
                               UserRepository userRepository,
                               ApplicationEventPublisher eventPublisher) {
        this.matchRequestRepository = matchRequestRepository;
        this.matchResponseRepository = matchResponseRepository;
        this.bookingRepository = bookingRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    // ========== MATCH REQUEST ==========

    /**
     * Tạo kèo - chủ kèo phải có booking đã CONFIRMED/DEPOSIT_PAID và là captain
     */
    @Transactional
    public MatchRequestResponse createMatchRequest(MatchRequestRequest request) {
        User user = getCurrentUser();

        // Validate booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        if (!booking.getCustomer().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không phải người đặt sân này");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.DEPOSIT_PAID) {
            throw new BadRequestException("Booking phải ở trạng thái CONFIRMED hoặc DEPOSIT_PAID để tạo kèo");
        }

        // Kiểm tra đã có kèo chưa
        matchRequestRepository.findByBookingId(booking.getId()).ifPresent(existing -> {
            throw new BadRequestException("Booking này đã có kèo ráp rồi");
        });

        // Validate team
        Team hostTeam = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.getTeamId()));

        if (!hostTeam.getCaptain().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn phải là đội trưởng để tạo kèo");
        }

        if (!hostTeam.getIsActive()) {
            throw new BadRequestException("Đội đã bị giải tán");
        }

        // Set cost sharing
        CostSharing costSharing = request.getCostSharing() != null ? request.getCostSharing() : CostSharing.EQUAL_SPLIT;
        BigDecimal hostShare = new BigDecimal("50.00");
        BigDecimal opponentShare = new BigDecimal("50.00");

        switch (costSharing) {
            case EQUAL_SPLIT:
                hostShare = new BigDecimal("50.00");
                opponentShare = new BigDecimal("50.00");
                break;
            case HOST_PAY:
                hostShare = new BigDecimal("100.00");
                opponentShare = BigDecimal.ZERO;
                break;
            case OPPONENT_PAY:
                hostShare = BigDecimal.ZERO;
                opponentShare = new BigDecimal("100.00");
                break;
            case CUSTOM:
                if (request.getHostSharePercent() != null && request.getOpponentSharePercent() != null) {
                    hostShare = request.getHostSharePercent();
                    opponentShare = request.getOpponentSharePercent();
                    if (hostShare.add(opponentShare).compareTo(new BigDecimal("100.00")) != 0) {
                        throw new BadRequestException("Tổng tỷ lệ chia phải bằng 100%");
                    }
                }
                break;
        }

        BigDecimal opponentAmount = booking.getTotalPrice()
                .multiply(opponentShare)
                .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);

        // Tính expired_at = booking date/time - 2 giờ
        LocalDateTime bookingDateTime = booking.getBookingDate()
                .atTime(booking.getTimeSlot().getStartTime());
        LocalDateTime expiredAt = bookingDateTime.minusHours(2);

        String matchCode = generateMatchCode();

        MatchRequest matchRequest = MatchRequest.builder()
                .matchCode(matchCode)
                .booking(booking)
                .hostTeam(hostTeam)
                .fieldType(booking.getField().getFieldType())
                .requiredSkillLevel(request.getRequiredSkillLevel() != null ? request.getRequiredSkillLevel() : SkillLevel.ANY)
                .costSharing(costSharing)
                .hostSharePercent(hostShare)
                .opponentSharePercent(opponentShare)
                .opponentAmount(opponentAmount)
                .message(request.getMessage())
                .contactPhone(request.getContactPhone())
                .expiredAt(expiredAt)
                .build();

        // Đánh dấu booking có match request
        booking.setIsMatchRequest(true);
        bookingRepository.save(booking);

        matchRequest = matchRequestRepository.save(matchRequest);
        publishMatchRequestCreatedEvent(matchRequest);
        return MatchRequestResponse.fromEntity(matchRequest);
    }

    /**
     * Danh sách kèo đang mở (public, filter)
     */
    @Transactional(readOnly = true)
    public Page<MatchRequestResponse> getOpenMatches(FieldType fieldType, SkillLevel skillLevel,
                                                      Long excludeUserId, Pageable pageable) {
        return matchRequestRepository.searchOpenMatches(fieldType, skillLevel, excludeUserId, pageable)
                .map(MatchRequestResponse::fromEntity);
    }

    /**
     * Chi tiết kèo (public)
     */
    @Transactional(readOnly = true)
    public MatchRequestResponse getMatchRequestDetail(Long id) {
        MatchRequest mr = matchRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MatchRequest", "id", id));

        List<MatchResponseResponse> responses = matchResponseRepository.findByMatchRequestId(id)
                .stream().map(MatchResponseResponse::fromEntity).toList();

        return MatchRequestResponse.fromEntityWithResponses(mr, responses);
    }

    /**
     * Hủy kèo - chỉ host captain
     */
    @Transactional
    public MatchRequestResponse cancelMatchRequest(Long id) {
        User user = getCurrentUser();

        MatchRequest mr = matchRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MatchRequest", "id", id));

        if (!mr.getHostTeam().getCaptain().getId().equals(user.getId())) {
            throw new UnauthorizedException("Chỉ đội trưởng chủ kèo mới có thể hủy");
        }

        if (mr.getStatus() != MatchStatus.OPEN) {
            throw new BadRequestException("Chỉ có thể hủy kèo đang mở");
        }

        mr.setStatus(MatchStatus.CANCELLED);
        matchRequestRepository.save(mr);

        // Từ chối tất cả responses đang pending
        List<MatchResponse> pendingResponses = matchResponseRepository
                .findByMatchRequestIdAndStatus(id, MatchResponseStatus.PENDING);
        for (MatchResponse resp : pendingResponses) {
            resp.setStatus(MatchResponseStatus.REJECTED);
        }
        matchResponseRepository.saveAll(pendingResponses);

        return MatchRequestResponse.fromEntity(mr);
    }

    /**
     * DS kèo tôi đã tạo
     */
    @Transactional(readOnly = true)
    public List<MatchRequestResponse> getMyCreatedMatches() {
        User user = getCurrentUser();
        List<Team> myTeams = teamRepository.findTeamsByMemberId(user.getId());

        return myTeams.stream()
                .filter(t -> t.getCaptain().getId().equals(user.getId()))
                .flatMap(t -> matchRequestRepository.findByHostTeamId(t.getId()).stream())
                .map(MatchRequestResponse::fromEntity)
                .toList();
    }

    /**
     * DS kèo tôi đã nhận (đội tôi đã gửi response)
     */
    @Transactional(readOnly = true)
    public List<MatchRequestResponse> getMyReceivedMatches() {
        User user = getCurrentUser();
        List<Team> myTeams = teamRepository.findTeamsByMemberId(user.getId());

        List<MatchRequestResponse> teamResponses = myTeams.stream()
                .flatMap(t -> matchResponseRepository.findByTeamId(t.getId()).stream())
                .map(resp -> MatchRequestResponse.fromEntity(resp.getMatchRequest()))
                .toList();

        List<MatchRequestResponse> individualResponses = matchResponseRepository.findByResponderUserId(user.getId())
                .stream()
                .map(resp -> MatchRequestResponse.fromEntity(resp.getMatchRequest()))
                .toList();

        return Stream.concat(teamResponses.stream(), individualResponses.stream())
                .collect(java.util.stream.Collectors.toMap(
                        MatchRequestResponse::getId,
                        r -> r,
                        (a, b) -> a,
                        LinkedHashMap::new))
                .values()
                .stream()
                .toList();
    }

    // ========== MATCH RESPONSE ==========

    /**
     * Gửi yêu cầu nhận kèo
     */
    @Transactional
    public MatchResponseResponse sendResponse(Long matchRequestId, MatchResponseRequest request) {
        User user = getCurrentUser();

        MatchRequest matchRequest = matchRequestRepository.findById(matchRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchRequest", "id", matchRequestId));

        if (matchRequest.getStatus() != MatchStatus.OPEN) {
            throw new BadRequestException("Kèo không ở trạng thái mở");
        }

        // Kiểm tra kèo đã expired chưa
        if (matchRequest.getExpiredAt() != null && LocalDateTime.now().isAfter(matchRequest.getExpiredAt())) {
            throw new BadRequestException("Kèo đã hết hạn");
        }

        MatchJoinType joinType = request.getJoinType() != null ? request.getJoinType()
                : (request.getTeamId() != null ? MatchJoinType.TEAM : MatchJoinType.INDIVIDUAL);

        MatchResponse response;
        if (joinType == MatchJoinType.TEAM) {
            if (request.getTeamId() == null) {
                throw new BadRequestException("Thiếu teamId khi nhận kèo theo đội");
            }

            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.getTeamId()));

            // Phải là captain của team
            if (!team.getCaptain().getId().equals(user.getId())) {
                throw new UnauthorizedException("Bạn phải là đội trưởng để nhận kèo");
            }

            if (!team.getIsActive()) {
                throw new BadRequestException("Đội đã bị giải tán");
            }

            // Không thể nhận kèo của chính mình
            if (team.getId().equals(matchRequest.getHostTeam().getId())) {
                throw new BadRequestException("Không thể nhận kèo của chính đội mình");
            }

            // Kiểm tra đã gửi response chưa
            if (matchResponseRepository.existsByMatchRequestIdAndTeamId(matchRequestId, team.getId())) {
                throw new BadRequestException("Đội đã gửi yêu cầu nhận kèo này rồi");
            }

            String contactPhone = request.getContactPhone();
            if (contactPhone == null || contactPhone.isBlank()) {
                contactPhone = team.getPhone() != null ? team.getPhone() : user.getPhone();
            }

            response = MatchResponse.builder()
                    .matchRequest(matchRequest)
                    .team(team)
                    .responderUser(user)
                    .joinType(MatchJoinType.TEAM)
                    .contactPhone(contactPhone)
                    .message(request.getMessage())
                    .respondedAt(LocalDateTime.now())
                    .build();
        } else {
            if (user.getId().equals(matchRequest.getHostTeam().getCaptain().getId())) {
                throw new BadRequestException("Không thể nhận kèo do chính mình tạo");
            }

            if (matchResponseRepository.existsByMatchRequestIdAndResponderUserId(matchRequestId, user.getId())) {
                throw new BadRequestException("Bạn đã gửi yêu cầu nhận kèo này rồi");
            }

            String contactPhone = request.getContactPhone();
            if (contactPhone == null || contactPhone.isBlank()) {
                contactPhone = user.getPhone();
            }
            if (contactPhone == null || contactPhone.isBlank()) {
                throw new BadRequestException("Vui lòng cung cấp số điện thoại liên hệ");
            }

            response = MatchResponse.builder()
                    .matchRequest(matchRequest)
                    .responderUser(user)
                    .joinType(MatchJoinType.INDIVIDUAL)
                    .contactPhone(contactPhone)
                    .message(request.getMessage())
                    .respondedAt(LocalDateTime.now())
                    .build();
        }

        response = matchResponseRepository.save(response);
        return MatchResponseResponse.fromEntity(response);
    }

    /**
     * Host captain chấp nhận 1 đội nhận kèo
     */
    @Transactional
    public MatchRequestResponse acceptResponse(Long matchRequestId, Long responseId) {
        User user = getCurrentUser();

        MatchRequest matchRequest = matchRequestRepository.findById(matchRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchRequest", "id", matchRequestId));

        if (!matchRequest.getHostTeam().getCaptain().getId().equals(user.getId())) {
            throw new UnauthorizedException("Chỉ đội trưởng chủ kèo mới có thể chấp nhận");
        }

        if (matchRequest.getStatus() != MatchStatus.OPEN) {
            throw new BadRequestException("Kèo không ở trạng thái mở");
        }

        MatchResponse acceptedResponse = matchResponseRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchResponse", "id", responseId));

        if (!acceptedResponse.getMatchRequest().getId().equals(matchRequestId)) {
            throw new BadRequestException("Response không thuộc kèo này");
        }

        if (acceptedResponse.getStatus() != MatchResponseStatus.PENDING) {
            throw new BadRequestException("Response không ở trạng thái chờ");
        }

        // Chấp nhận response đã chọn
        acceptedResponse.setStatus(MatchResponseStatus.ACCEPTED);
        matchResponseRepository.save(acceptedResponse);

        // Từ chối tất cả responses khác
        List<MatchResponse> otherResponses = matchResponseRepository
                .findByMatchRequestIdAndStatus(matchRequestId, MatchResponseStatus.PENDING);
        for (MatchResponse resp : otherResponses) {
            if (!resp.getId().equals(responseId)) {
                resp.setStatus(MatchResponseStatus.REJECTED);
            }
        }
        matchResponseRepository.saveAll(otherResponses);

        // Cập nhật match request
        matchRequest.setStatus(MatchStatus.ACCEPTED);
        matchRequest.setOpponentTeam(acceptedResponse.getTeam());
        matchRequest.setAcceptedAt(LocalDateTime.now());
        matchRequestRepository.save(matchRequest);

        List<MatchResponseResponse> allResponses = matchResponseRepository.findByMatchRequestId(matchRequestId)
                .stream().map(MatchResponseResponse::fromEntity).toList();

        return MatchRequestResponse.fromEntityWithResponses(matchRequest, allResponses);
    }

    /**
     * Host captain từ chối 1 đội nhận kèo
     */
    @Transactional
    public MatchResponseResponse rejectResponse(Long matchRequestId, Long responseId) {
        User user = getCurrentUser();

        MatchRequest matchRequest = matchRequestRepository.findById(matchRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchRequest", "id", matchRequestId));

        if (!matchRequest.getHostTeam().getCaptain().getId().equals(user.getId())) {
            throw new UnauthorizedException("Chỉ đội trưởng chủ kèo mới có thể từ chối");
        }

        if (matchRequest.getStatus() != MatchStatus.OPEN) {
            throw new BadRequestException("Kèo không ở trạng thái mở");
        }

        MatchResponse response = matchResponseRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchResponse", "id", responseId));

        if (!response.getMatchRequest().getId().equals(matchRequestId)) {
            throw new BadRequestException("Response không thuộc kèo này");
        }

        if (response.getStatus() != MatchResponseStatus.PENDING) {
            throw new BadRequestException("Response không ở trạng thái chờ");
        }

        response.setStatus(MatchResponseStatus.REJECTED);
        matchResponseRepository.save(response);

        return MatchResponseResponse.fromEntity(response);
    }

    /**
     * Đội rút khỏi kèo (withdraw response)
     */
    @Transactional
    public MatchResponseResponse withdrawResponse(Long matchRequestId, Long responseId) {
        User user = getCurrentUser();

        MatchResponse response = matchResponseRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchResponse", "id", responseId));

        if (!response.getMatchRequest().getId().equals(matchRequestId)) {
            throw new BadRequestException("Response không thuộc kèo này");
        }

        if (response.getJoinType() == MatchJoinType.TEAM) {
            if (response.getTeam() == null || !response.getTeam().getCaptain().getId().equals(user.getId())) {
                throw new UnauthorizedException("Chỉ đội trưởng mới có thể rút kèo");
            }
        } else {
            if (response.getResponderUser() == null || !response.getResponderUser().getId().equals(user.getId())) {
                throw new UnauthorizedException("Chỉ người gửi yêu cầu mới có thể rút kèo");
            }
        }

        if (response.getStatus() != MatchResponseStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể rút khi đang chờ");
        }

        response.setStatus(MatchResponseStatus.WITHDRAWN);
        matchResponseRepository.save(response);

        return MatchResponseResponse.fromEntity(response);
    }

    // ========== HELPERS ==========

    private String generateMatchCode() {
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "MR" + uuid;
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
    }

    private void publishMatchRequestCreatedEvent(MatchRequest matchRequest) {
        eventPublisher.publishEvent(MatchRequestCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .matchRequestId(matchRequest.getId())
                .matchCode(matchRequest.getMatchCode())
                .stadiumName(matchRequest.getBooking().getField().getStadium().getName())
                .fieldName(matchRequest.getBooking().getField().getName())
                .bookingDate(matchRequest.getBooking().getBookingDate())
                .startTime(matchRequest.getBooking().getTimeSlot().getStartTime())
                .endTime(matchRequest.getBooking().getTimeSlot().getEndTime())
                .hostTeamName(matchRequest.getHostTeam().getName())
                .contactPhone(matchRequest.getContactPhone())
                .build());
    }
}
