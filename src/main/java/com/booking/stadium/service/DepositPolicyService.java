package com.booking.stadium.service;

import com.booking.stadium.dto.deposit.DepositPolicyRequest;
import com.booking.stadium.dto.deposit.DepositPolicyResponse;
import com.booking.stadium.entity.DepositPolicy;
import com.booking.stadium.entity.Stadium;
import com.booking.stadium.entity.User;
import com.booking.stadium.exception.ResourceNotFoundException;
import com.booking.stadium.exception.UnauthorizedException;
import com.booking.stadium.repository.DepositPolicyRepository;
import com.booking.stadium.repository.StadiumRepository;
import com.booking.stadium.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositPolicyService {

    private final DepositPolicyRepository depositPolicyRepository;
    private final StadiumRepository stadiumRepository;
    private final UserRepository userRepository;

    public DepositPolicyService(DepositPolicyRepository depositPolicyRepository,
                                StadiumRepository stadiumRepository,
                                UserRepository userRepository) {
        this.depositPolicyRepository = depositPolicyRepository;
        this.stadiumRepository = stadiumRepository;
        this.userRepository = userRepository;
    }

    // ========== PUBLIC ==========

    @Transactional(readOnly = true)
    public DepositPolicyResponse getDepositPolicy(Long stadiumId) {
        DepositPolicy policy = depositPolicyRepository.findByStadiumId(stadiumId)
                .orElseThrow(() -> new ResourceNotFoundException("DepositPolicy", "stadiumId", stadiumId));
        return DepositPolicyResponse.fromEntity(policy);
    }

    // ========== OWNER ==========

    @Transactional
    public DepositPolicyResponse createOrUpdateDepositPolicy(Long stadiumId, DepositPolicyRequest request) {
        Stadium stadium = getOwnedStadium(stadiumId);

        DepositPolicy policy = depositPolicyRepository.findByStadiumId(stadiumId)
                .orElse(DepositPolicy.builder().stadium(stadium).build());

        policy.setDepositPercent(request.getDepositPercent());
        policy.setRefundBeforeHours(request.getRefundBeforeHours());
        policy.setRefundPercent(request.getRefundPercent());
        policy.setLateCancelRefundPercent(request.getLateCancelRefundPercent());
        policy.setRecurringDiscountPercent(request.getRecurringDiscountPercent());
        policy.setMinRecurringSessions(request.getMinRecurringSessions());
        policy.setIsDepositRequired(request.getIsDepositRequired());

        policy = depositPolicyRepository.save(policy);
        return DepositPolicyResponse.fromEntity(policy);
    }

    // ========== HELPERS ==========
    @Transactional(readOnly = true)
    private Stadium getOwnedStadium(Long stadiumId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User owner = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));

        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new ResourceNotFoundException("Stadium", "id", stadiumId));

        if (!stadium.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Bạn không có quyền quản lý sân này");
        }
        return stadium;
    }
}
