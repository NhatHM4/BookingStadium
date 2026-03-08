package com.booking.stadium.service;

import com.booking.stadium.dto.review.ReviewRequest;
import com.booking.stadium.dto.review.ReviewResponse;
import com.booking.stadium.entity.Booking;
import com.booking.stadium.entity.Review;
import com.booking.stadium.entity.User;
import com.booking.stadium.enums.BookingStatus;
import com.booking.stadium.exception.BadRequestException;
import com.booking.stadium.exception.ResourceNotFoundException;
import com.booking.stadium.repository.BookingRepository;
import com.booking.stadium.repository.ReviewRepository;
import com.booking.stadium.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         BookingRepository bookingRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    /**
     * Tạo đánh giá (chỉ khi booking COMPLETED)
     */
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        User customer = getCurrentUser();

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        // Chỉ customer của booking mới được đánh giá
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Bạn không phải người đặt sân này");
        }

        // Chỉ đánh giá khi booking COMPLETED
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("Chỉ được đánh giá khi booking đã hoàn thành");
        }

        // Kiểm tra đã đánh giá chưa
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new BadRequestException("Bạn đã đánh giá booking này rồi");
        }

        Review review = Review.builder()
                .booking(booking)
                .customer(customer)
                .stadium(booking.getField().getStadium())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        review = reviewRepository.save(review);
        return ReviewResponse.fromEntity(review);
    }

    /**
     * Danh sách đánh giá của sân (public)
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getStadiumReviews(Long stadiumId, Pageable pageable) {
        return reviewRepository.findByStadiumId(stadiumId, pageable)
                .map(ReviewResponse::fromEntity);
    }

    /**
     * Danh sách đánh giá của tôi
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(Pageable pageable) {
        User customer = getCurrentUser();
        return reviewRepository.findByCustomerId(customer.getId(), pageable)
                .map(ReviewResponse::fromEntity);
    }

    /**
     * Xóa đánh giá (chủ đánh giá hoặc admin)
     */
    @Transactional
    public void deleteReview(Long reviewId) {
        User user = getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getCustomer().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền xóa đánh giá này");
        }

        reviewRepository.delete(review);
    }

    // ========== HELPERS ==========

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
    }
}
