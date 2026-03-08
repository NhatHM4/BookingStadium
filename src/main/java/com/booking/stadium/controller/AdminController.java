package com.booking.stadium.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.dto.admin.DashboardResponse;
import com.booking.stadium.dto.auth.UserResponse;
import com.booking.stadium.enums.Role;
import com.booking.stadium.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "APIs quản trị hệ thống (chỉ Admin)")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ========== USER MANAGEMENT ==========

    /**
     * Danh sách users (phân trang, filter role, search name/email)
     */
    @GetMapping("/users")
    @Operation(summary = "DS users (filter role, search, paging)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Thành công",
                adminService.getUsers(role, search, pageable)));
    }

    /**
     * Chi tiết user
     */
    @GetMapping("/users/{id}")
    @Operation(summary = "Chi tiết user")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Thành công",
                adminService.getUserById(id)));
    }

    /**
     * Bật/tắt user
     */
    @PutMapping("/users/{id}/toggle-active")
    @Operation(summary = "Bật/tắt tài khoản user")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserActive(@PathVariable Long id) {
        UserResponse response = adminService.toggleUserActive(id);
        String message = response.getIsActive() ? "Kích hoạt tài khoản thành công" : "Vô hiệu hóa tài khoản thành công";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    // ========== DASHBOARD ==========

    /**
     * Thống kê tổng quan
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard thống kê tổng quan")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Thành công",
                adminService.getDashboard()));
    }
}
