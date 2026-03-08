package com.booking.stadium.service;

import com.booking.stadium.dto.admin.DashboardResponse;
import com.booking.stadium.dto.auth.UserResponse;
import com.booking.stadium.entity.User;
import com.booking.stadium.enums.*;
import com.booking.stadium.exception.BadRequestException;
import com.booking.stadium.exception.ResourceNotFoundException;
import com.booking.stadium.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final StadiumRepository stadiumRepository;
    private final BookingRepository bookingRepository;
    private final TeamRepository teamRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final ReviewRepository reviewRepository;

    public AdminService(UserRepository userRepository,
                        StadiumRepository stadiumRepository,
                        BookingRepository bookingRepository,
                        TeamRepository teamRepository,
                        MatchRequestRepository matchRequestRepository,
                        ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.stadiumRepository = stadiumRepository;
        this.bookingRepository = bookingRepository;
        this.teamRepository = teamRepository;
        this.matchRequestRepository = matchRequestRepository;
        this.reviewRepository = reviewRepository;
    }

    // ========== USER MANAGEMENT ==========

    /**
     * Danh sách users (phân trang, filter theo role, search)
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Role role, String search, Pageable pageable) {
        return userRepository.searchUsers(role, search, pageable)
                .map(UserResponse::fromEntity);
    }

    /**
     * Chi tiết user
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserResponse.fromEntity(user);
    }

    /**
     * Bật/tắt user (toggle active)
     */
    @Transactional
    public UserResponse toggleUserActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Không thể vô hiệu hóa tài khoản admin");
        }

        user.setIsActive(!user.getIsActive());
        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    // ========== DASHBOARD ==========

    /**
     * Thống kê tổng quan cho admin
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        // User stats
        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long totalOwners = userRepository.countByRole(Role.OWNER);

        // Stadium stats
        long totalStadiums = stadiumRepository.count();
        long approvedStadiums = stadiumRepository.countByStatus(StadiumStatus.APPROVED);
        long pendingStadiums = stadiumRepository.countByStatus(StadiumStatus.PENDING);

        // Booking stats
        long totalBookings = bookingRepository.count();
        long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        // Team & Match stats
        long totalTeams = teamRepository.count();
        long totalMatchRequests = matchRequestRepository.count();

        // Review stats
        long totalReviews = reviewRepository.count();
        Double averageRating = reviewRepository.getOverallAverageRating();

        // Booking trong 7 ngày gần nhất
        Map<String, Long> recentBookings = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            long count = bookingRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            recentBookings.put(date.toString(), count);
        }

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalCustomers(totalCustomers)
                .totalOwners(totalOwners)
                .totalStadiums(totalStadiums)
                .approvedStadiums(approvedStadiums)
                .pendingStadiums(pendingStadiums)
                .totalBookings(totalBookings)
                .completedBookings(completedBookings)
                .cancelledBookings(cancelledBookings)
                .totalTeams(totalTeams)
                .totalMatchRequests(totalMatchRequests)
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .recentBookings(recentBookings)
                .build();
    }
}
