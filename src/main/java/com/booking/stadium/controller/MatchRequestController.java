package com.booking.stadium.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.dto.match.MatchRequestRequest;
import com.booking.stadium.dto.match.MatchRequestResponse;
import com.booking.stadium.dto.match.MatchResponseRequest;
import com.booking.stadium.dto.match.MatchResponseResponse;
import com.booking.stadium.enums.FieldType;
import com.booking.stadium.enums.SkillLevel;
import com.booking.stadium.service.MatchRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Match Making", description = "APIs kèo ráp đối")
public class MatchRequestController {

    private final MatchRequestService matchRequestService;

    public MatchRequestController(MatchRequestService matchRequestService) {
        this.matchRequestService = matchRequestService;
    }

    // ========== MATCH REQUEST ==========

    /**
     * Tạo kèo ráp
     */
    @PostMapping("/match-requests")
    @Operation(summary = "Tạo kèo ráp đối")
    public ResponseEntity<ApiResponse<MatchRequestResponse>> createMatchRequest(
            @Valid @RequestBody MatchRequestRequest request) {
        MatchRequestResponse response = matchRequestService.createMatchRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo kèo thành công", response));
    }

    /**
     * Danh sách kèo đang mở (public, filter)
     */
    @GetMapping("/match-requests")
    @Operation(summary = "DS kèo đang mở (public, filter)")
    public ResponseEntity<ApiResponse<Page<MatchRequestResponse>>> getOpenMatches(
            @RequestParam(required = false) FieldType fieldType,
            @RequestParam(required = false) SkillLevel skillLevel,
            @RequestParam(required = false) Long excludeUserId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Thành công",
                matchRequestService.getOpenMatches(fieldType, skillLevel, excludeUserId, pageable)));
    }

    /**
     * DS kèo tôi đã tạo
     */
    @GetMapping("/match-requests/my")
    @Operation(summary = "DS kèo tôi đã tạo")
    public ResponseEntity<ApiResponse<List<MatchRequestResponse>>> getMyCreatedMatches() {
        return ResponseEntity.ok(ApiResponse.success("Thành công",
                matchRequestService.getMyCreatedMatches()));
    }

    /**
     * DS kèo tôi đã nhận
     */
    @GetMapping("/match-requests/my-matches")
    @Operation(summary = "DS kèo tôi đã nhận")
    public ResponseEntity<ApiResponse<List<MatchRequestResponse>>> getMyReceivedMatches() {
        return ResponseEntity.ok(ApiResponse.success("Thành công",
                matchRequestService.getMyReceivedMatches()));
    }

    /**
     * Chi tiết kèo (public)
     */
    @GetMapping("/match-requests/{id}")
    @Operation(summary = "Chi tiết kèo")
    public ResponseEntity<ApiResponse<MatchRequestResponse>> getMatchRequestDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Thành công",
                matchRequestService.getMatchRequestDetail(id)));
    }

    /**
     * Hủy kèo (host captain)
     */
    @PutMapping("/match-requests/{id}/cancel")
    @Operation(summary = "Hủy kèo (host captain)")
    public ResponseEntity<ApiResponse<MatchRequestResponse>> cancelMatchRequest(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Hủy kèo thành công",
                matchRequestService.cancelMatchRequest(id)));
    }

    // ========== MATCH RESPONSE ==========

    /**
     * Gửi yêu cầu nhận kèo
     */
    @PostMapping("/match-requests/{id}/responses")
    @Operation(summary = "Gửi yêu cầu nhận kèo")
    public ResponseEntity<ApiResponse<MatchResponseResponse>> sendResponse(
            @PathVariable Long id,
            @RequestBody MatchResponseRequest request) {
        MatchResponseResponse response = matchRequestService.sendResponse(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Gửi yêu cầu nhận kèo thành công", response));
    }

    /**
     * Chấp nhận đội nhận kèo (host captain)
     */
    @PutMapping("/match-requests/{id}/responses/{responseId}/accept")
    @Operation(summary = "Chấp nhận đội nhận kèo")
    public ResponseEntity<ApiResponse<MatchRequestResponse>> acceptResponse(
            @PathVariable Long id,
            @PathVariable Long responseId) {
        return ResponseEntity.ok(ApiResponse.success("Chấp nhận đội nhận kèo thành công",
                matchRequestService.acceptResponse(id, responseId)));
    }

    /**
     * Từ chối đội nhận kèo (host captain)
     */
    @PutMapping("/match-requests/{id}/responses/{responseId}/reject")
    @Operation(summary = "Từ chối đội nhận kèo")
    public ResponseEntity<ApiResponse<MatchResponseResponse>> rejectResponse(
            @PathVariable Long id,
            @PathVariable Long responseId) {
        return ResponseEntity.ok(ApiResponse.success("Từ chối đội nhận kèo thành công",
                matchRequestService.rejectResponse(id, responseId)));
    }

    /**
     * Rút khỏi kèo (team captain)
     */
    @PutMapping("/match-requests/{id}/responses/{responseId}/withdraw")
    @Operation(summary = "Rút khỏi kèo")
    public ResponseEntity<ApiResponse<MatchResponseResponse>> withdrawResponse(
            @PathVariable Long id,
            @PathVariable Long responseId) {
        return ResponseEntity.ok(ApiResponse.success("Rút kèo thành công",
                matchRequestService.withdrawResponse(id, responseId)));
    }
}
