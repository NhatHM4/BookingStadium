package com.booking.stadium.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.dto.stadium.StadiumRequest;
import com.booking.stadium.dto.stadium.StadiumResponse;
import com.booking.stadium.service.StadiumService;
import com.booking.stadium.enums.FieldType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Stadium", description = "APIs quản lý sân bóng")
public class StadiumController {

    private final StadiumService stadiumService;

    public StadiumController(StadiumService stadiumService) {
        this.stadiumService = stadiumService;
    }

    // ========== PUBLIC ==========

    @GetMapping("/stadiums")
    @Operation(summary = "Danh sách sân (public, filter, paging)")
    public ResponseEntity<ApiResponse<Page<StadiumResponse>>> getStadiums(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) FieldType fieldType,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<StadiumResponse> result = stadiumService.getApprovedStadiums(city, district, name, fieldType, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/stadiums/{id}")
    @Operation(summary = "Chi tiết sân")
    public ResponseEntity<ApiResponse<StadiumResponse>> getStadium(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(stadiumService.getStadiumById(id)));
    }

    @GetMapping("/stadiums/nearby")
    @Operation(summary = "Tìm sân gần đây")
    public ResponseEntity<ApiResponse<List<StadiumResponse>>> getNearbyStadiums(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "5") Double radius) {
        return ResponseEntity.ok(ApiResponse.success(stadiumService.getNearbyStadiums(lat, lng, radius)));
    }

    // ========== OWNER ==========

    @PostMapping("/stadiums")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Tạo sân mới (Owner) - Gửi imageUrl từ API upload")
    public ResponseEntity<ApiResponse<StadiumResponse>> createStadium(
            @Valid @RequestBody StadiumRequest request) {
        StadiumResponse response = stadiumService.createStadium(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo sân thành công, chờ admin duyệt", response));
    }

    @PutMapping("/stadiums/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Cập nhật sân (Owner)")
    public ResponseEntity<ApiResponse<StadiumResponse>> updateStadium(
            @PathVariable Long id,
            @Valid @RequestBody StadiumRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", stadiumService.updateStadium(id, request)));
    }

    @DeleteMapping("/stadiums/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Xóa sân (Owner)")
    public ResponseEntity<ApiResponse<Void>> deleteStadium(@PathVariable Long id) {
        stadiumService.deleteStadium(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sân thành công"));
    }

    @GetMapping("/owner/stadiums")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "DS sân của tôi (Owner)")
    public ResponseEntity<ApiResponse<List<StadiumResponse>>> getMyStadiums() {
        return ResponseEntity.ok(ApiResponse.success(stadiumService.getMyStadiums()));
    }

    // ========== ADMIN ==========

    @GetMapping("/admin/stadiums/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "DS sân chờ duyệt (Admin)")
    public ResponseEntity<ApiResponse<Page<StadiumResponse>>> getPendingStadiums(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(stadiumService.getPendingStadiums(pageable)));
    }

    @PutMapping("/admin/stadiums/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Duyệt sân (Admin)")
    public ResponseEntity<ApiResponse<StadiumResponse>> approveStadium(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Duyệt sân thành công", stadiumService.approveStadium(id)));
    }

    @PutMapping("/admin/stadiums/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Từ chối sân (Admin)")
    public ResponseEntity<ApiResponse<StadiumResponse>> rejectStadium(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Từ chối sân thành công", stadiumService.rejectStadium(id)));
    }
}
