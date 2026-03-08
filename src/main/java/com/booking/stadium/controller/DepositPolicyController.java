package com.booking.stadium.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.dto.deposit.DepositPolicyRequest;
import com.booking.stadium.dto.deposit.DepositPolicyResponse;
import com.booking.stadium.service.DepositPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Deposit Policy", description = "APIs chính sách đặt cọc")
public class DepositPolicyController {

    private final DepositPolicyService depositPolicyService;

    public DepositPolicyController(DepositPolicyService depositPolicyService) {
        this.depositPolicyService = depositPolicyService;
    }

    @GetMapping("/stadiums/{stadiumId}/deposit-policy")
    @Operation(summary = "Xem chính sách cọc của sân")
    public ResponseEntity<ApiResponse<DepositPolicyResponse>> getDepositPolicy(@PathVariable Long stadiumId) {
        return ResponseEntity.ok(ApiResponse.success(depositPolicyService.getDepositPolicy(stadiumId)));
    }

    @PutMapping("/stadiums/{stadiumId}/deposit-policy")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Cập nhật chính sách cọc (Owner)")
    public ResponseEntity<ApiResponse<DepositPolicyResponse>> updateDepositPolicy(
            @PathVariable Long stadiumId,
            @Valid @RequestBody DepositPolicyRequest request) {
        DepositPolicyResponse response = depositPolicyService.createOrUpdateDepositPolicy(stadiumId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chính sách cọc thành công", response));
    }
}
