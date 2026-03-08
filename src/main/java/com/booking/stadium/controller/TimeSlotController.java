package com.booking.stadium.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.dto.timeslot.TimeSlotRequest;
import com.booking.stadium.dto.timeslot.TimeSlotResponse;
import com.booking.stadium.service.TimeSlotService;
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
@Tag(name = "TimeSlot", description = "APIs quản lý khung giờ")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @GetMapping("/fields/{fieldId}/time-slots")
    @Operation(summary = "DS khung giờ của sân con")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getTimeSlots(@PathVariable Long fieldId) {
        return ResponseEntity.ok(ApiResponse.success(timeSlotService.getTimeSlotsByField(fieldId)));
    }

    @PostMapping("/fields/{fieldId}/time-slots")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Tạo khung giờ (Owner)")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> createTimeSlot(
            @PathVariable Long fieldId,
            @Valid @RequestBody TimeSlotRequest request) {
        TimeSlotResponse response = timeSlotService.createTimeSlot(fieldId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo khung giờ thành công", response));
    }

    @PutMapping("/time-slots/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Cập nhật khung giờ (Owner)")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> updateTimeSlot(
            @PathVariable Long id,
            @Valid @RequestBody TimeSlotRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", timeSlotService.updateTimeSlot(id, request)));
    }

    @DeleteMapping("/time-slots/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Xóa khung giờ (Owner)")
    public ResponseEntity<ApiResponse<Void>> deleteTimeSlot(@PathVariable Long id) {
        timeSlotService.deleteTimeSlot(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa khung giờ thành công"));
    }
}
