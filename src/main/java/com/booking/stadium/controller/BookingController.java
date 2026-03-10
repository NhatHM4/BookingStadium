package com.booking.stadium.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.dto.booking.AvailableSlotResponse;
import com.booking.stadium.dto.booking.BookingRequest;
import com.booking.stadium.dto.booking.BookingResponse;
import com.booking.stadium.enums.BookingStatus;
import com.booking.stadium.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Booking", description = "APIs đặt sân")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // ========== PUBLIC ==========

    @GetMapping("/fields/{fieldId}/available-slots")
    @Operation(summary = "Xem slot trống theo ngày")
    public ResponseEntity<ApiResponse<List<AvailableSlotResponse>>> getAvailableSlots(
            @PathVariable Long fieldId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getAvailableSlots(fieldId, date)));
    }

    @PostMapping("/bookings/guest")
    @Operation(summary = "Đặt sân cho khách (không cần đăng nhập)")
    public ResponseEntity<ApiResponse<BookingResponse>> createGuestBooking(
            @Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createGuestBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt sân thành công (Khách)", response));
    }

    @GetMapping("/bookings/lookup")
    @Operation(summary = "Tra cứu đơn đặt sân theo mã booking (không cần đăng nhập)")
    public ResponseEntity<ApiResponse<BookingResponse>> lookupBooking(
            @RequestParam String bookingCode) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingByCode(bookingCode)));
    }

    // ========== CUSTOMER ==========

    @PostMapping("/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Đặt sân")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt sân thành công", response));
    }

    @GetMapping("/bookings/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Lịch sử đặt sân của tôi")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getMyBookings(status, pageable)));
    }

    @GetMapping("/bookings/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Chi tiết đơn đặt")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingById(id)));
    }

    @PutMapping("/bookings/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Hủy đặt sân")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Hủy đặt sân thành công", bookingService.cancelBooking(id)));
    }

    // ========== OWNER ==========

    @GetMapping("/owner/bookings")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "DS đơn đặt của sân (Owner)")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getOwnerBookings(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getOwnerBookings(pageable)));
    }

    @PutMapping("/owner/bookings/{id}/confirm")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Xác nhận đơn đặt (Owner)")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Xác nhận thành công", bookingService.confirmBooking(id)));
    }

    @PutMapping("/owner/bookings/{id}/reject")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Từ chối đơn đặt (Owner)")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBooking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Từ chối thành công", bookingService.rejectBooking(id)));
    }

    @PutMapping("/owner/bookings/{id}/complete")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Hoàn thành đơn đặt (Owner)")
    public ResponseEntity<ApiResponse<BookingResponse>> completeBooking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Hoàn thành thành công", bookingService.completeBooking(id)));
    }

    @GetMapping("/owner/stadiums/{stadiumId}/bookings")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "DS booking theo sân và ngày (Owner)")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsByStadiumAndDate(
            @PathVariable Long stadiumId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingsByStadiumAndDate(stadiumId, date)));
    }
}
