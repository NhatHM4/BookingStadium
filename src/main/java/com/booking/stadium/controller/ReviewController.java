package com.booking.stadium.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.dto.review.ReviewRequest;
import com.booking.stadium.dto.review.ReviewResponse;
import com.booking.stadium.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Review", description = "APIs đánh giá sân")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Tạo đánh giá (Customer, booking phải COMPLETED)
     */
    @PostMapping("/reviews")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER')")
    @Operation(summary = "Tạo đánh giá (booking phải COMPLETED)")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đánh giá thành công", response));
    }

    /**
     * DS đánh giá của sân (public)
     */
    @GetMapping("/stadiums/{stadiumId}/reviews")
    @Operation(summary = "DS đánh giá của sân (public)")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getStadiumReviews(
            @PathVariable Long stadiumId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Thành công",
                reviewService.getStadiumReviews(stadiumId, pageable)));
    }

    /**
     * DS đánh giá của tôi
     */
    @GetMapping("/reviews/my")
    @Operation(summary = "DS đánh giá của tôi")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getMyReviews(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Thành công",
                reviewService.getMyReviews(pageable)));
    }

    /**
     * Xóa đánh giá
     */
    @DeleteMapping("/reviews/{id}")
    @Operation(summary = "Xóa đánh giá")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa đánh giá thành công"));
    }
}
