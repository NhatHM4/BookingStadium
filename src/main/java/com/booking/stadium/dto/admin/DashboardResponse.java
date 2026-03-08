package com.booking.stadium.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Long totalUsers;
    private Long totalCustomers;
    private Long totalOwners;
    private Long totalStadiums;
    private Long approvedStadiums;
    private Long pendingStadiums;
    private Long totalBookings;
    private Long completedBookings;
    private Long cancelledBookings;
    private Long totalTeams;
    private Long totalMatchRequests;
    private Long totalReviews;
    private Double averageRating;

    // Thống kê booking theo tháng (7 ngày gần nhất)
    private Map<String, Long> recentBookings;
}
