package com.booking.stadium.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.dto.recurring.RecurringBookingRequest;
import com.booking.stadium.dto.recurring.RecurringBookingResponse;
import com.booking.stadium.enums.RecurringBookingStatus;
import com.booking.stadium.service.RecurringBookingService;
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
@Tag(name = "Recurring Booking", description = "APIs đặt sân dài hạn")
public class RecurringBookingController {

    private final RecurringBookingService recurringBookingService;

    public RecurringBookingController(RecurringBookingService recurringBookingService) {
        this.recurringBookingService = recurringBookingService;
    }

    // ========== CUSTOMER ==========

    @PostMapping("/recurring-bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Đặt sân dài hạn")
    public ResponseEntity<ApiResponse<RecurringBookingResponse>> createRecurringBooking(
            @Valid @RequestBody RecurringBookingRequest request) {
        RecurringBookingResponse response = recurringBookingService.createRecurringBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo gói đặt sân dài hạn thành công", response));
    }

    @GetMapping("/recurring-bookings/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "DS gói dài hạn của tôi")
    public ResponseEntity<ApiResponse<Page<RecurringBookingResponse>>> getMyRecurringBookings(
            @RequestParam(required = false) RecurringBookingStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RecurringBookingResponse> response = recurringBookingService.getMyRecurringBookings(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recurring-bookings/{id}")
    @Operation(summary = "Chi tiết gói dài hạn kèm DS các buổi")
    public ResponseEntity<ApiResponse<RecurringBookingResponse>> getRecurringBookingDetail(
            @PathVariable Long id) {
        RecurringBookingResponse response = recurringBookingService.getRecurringBookingDetail(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/recurring-bookings/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Hủy toàn bộ gói dài hạn")
    public ResponseEntity<ApiResponse<RecurringBookingResponse>> cancelRecurringBooking(
            @PathVariable Long id) {
        RecurringBookingResponse response = recurringBookingService.cancelRecurringBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Hủy gói dài hạn thành công", response));
    }

    @PutMapping("/recurring-bookings/{id}/bookings/{bookingId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Hủy 1 buổi trong gói dài hạn")
    public ResponseEntity<ApiResponse<RecurringBookingResponse>> cancelSingleBooking(
            @PathVariable Long id,
            @PathVariable Long bookingId) {
        RecurringBookingResponse response = recurringBookingService.cancelSingleBooking(id, bookingId);
        return ResponseEntity.ok(ApiResponse.success("Hủy buổi thành công", response));
    }

    // ========== OWNER ==========

    @GetMapping("/owner/recurring-bookings")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "DS gói dài hạn của sân mình")
    public ResponseEntity<ApiResponse<Page<RecurringBookingResponse>>> getOwnerRecurringBookings(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RecurringBookingResponse> response = recurringBookingService.getOwnerRecurringBookings(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/owner/recurring-bookings/{id}/confirm")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Xác nhận gói dài hạn")
    public ResponseEntity<ApiResponse<RecurringBookingResponse>> confirmRecurringBooking(
            @PathVariable Long id) {
        RecurringBookingResponse response = recurringBookingService.confirmRecurringBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận gói dài hạn thành công", response));
    }
}
