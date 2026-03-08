package com.booking.stadium.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.dto.deposit.DepositRequest;
import com.booking.stadium.dto.deposit.DepositResponse;
import com.booking.stadium.dto.deposit.RefundRequest;
import com.booking.stadium.service.DepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Deposit", description = "APIs quản lý đặt cọc")
public class DepositController {

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    // ========== CUSTOMER ==========

    @PostMapping("/bookings/{bookingId}/deposits")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Tạo giao dịch đặt cọc cho booking")
    public ResponseEntity<ApiResponse<DepositResponse>> createDeposit(
            @PathVariable Long bookingId,
            @Valid @RequestBody DepositRequest request) {
        DepositResponse response = depositService.createDeposit(bookingId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo giao dịch cọc thành công", response));
    }

    @GetMapping("/bookings/{bookingId}/deposits")
    @Operation(summary = "Xem lịch sử giao dịch cọc của booking")
    public ResponseEntity<ApiResponse<List<DepositResponse>>> getDepositsByBooking(
            @PathVariable Long bookingId) {
        List<DepositResponse> response = depositService.getDepositsByBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== OWNER ==========

    @PutMapping("/owner/deposits/{id}/confirm")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Xác nhận đã nhận tiền cọc")
    public ResponseEntity<ApiResponse<DepositResponse>> confirmDeposit(@PathVariable Long id) {
        DepositResponse response = depositService.confirmDeposit(id);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận cọc thành công", response));
    }

    @PutMapping("/owner/deposits/{id}/reject")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Từ chối giao dịch cọc")
    public ResponseEntity<ApiResponse<DepositResponse>> rejectDeposit(@PathVariable Long id) {
        DepositResponse response = depositService.rejectDeposit(id);
        return ResponseEntity.ok(ApiResponse.success("Từ chối giao dịch cọc", response));
    }

    @PostMapping("/owner/bookings/{bookingId}/refund")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Hoàn cọc khi hủy booking")
    public ResponseEntity<ApiResponse<DepositResponse>> refundDeposit(
            @PathVariable Long bookingId,
            @RequestBody(required = false) RefundRequest request) {
        if (request == null) {
            request = new RefundRequest();
        }
        DepositResponse response = depositService.refundDeposit(bookingId, request);
        return ResponseEntity.ok(ApiResponse.success("Hoàn cọc thành công", response));
    }
}
