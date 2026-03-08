package com.booking.stadium.dto.review;

import com.booking.stadium.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long bookingId;
    private String bookingCode;
    private Long customerId;
    private String customerName;
    private String customerAvatarUrl;
    private Long stadiumId;
    private String stadiumName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .bookingCode(review.getBooking().getBookingCode())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getFullName())
                .customerAvatarUrl(review.getCustomer().getAvatarUrl())
                .stadiumId(review.getStadium().getId())
                .stadiumName(review.getStadium().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
