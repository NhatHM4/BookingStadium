package com.booking.stadium.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.dto.team.AddMemberRequest;
import com.booking.stadium.dto.team.TeamRequest;
import com.booking.stadium.dto.team.TeamResponse;
import com.booking.stadium.dto.team.TeamMemberResponse;
import com.booking.stadium.service.TeamService;
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
@Tag(name = "Team", description = "APIs quản lý đội bóng")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * Tạo đội bóng mới
     */
    @PostMapping("/teams")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER')")
    @Operation(summary = "Tạo đội bóng mới")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@Valid @RequestBody TeamRequest request) {
        TeamResponse team = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo đội thành công", team));
    }

    /**
     * Danh sách đội của tôi
     */
    @GetMapping("/teams/my")
    @Operation(summary = "DS đội bóng của tôi")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getMyTeams() {
        return ResponseEntity.ok(ApiResponse.success("Thành công", teamService.getMyTeams()));
    }

    /**
     * Chi tiết đội (kèm danh sách thành viên)
     */
    @GetMapping("/teams/{id}")
    @Operation(summary = "Chi tiết đội bóng")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Thành công", teamService.getTeamDetail(id)));
    }

    /**
     * Cập nhật thông tin đội (captain only)
     */
    @PutMapping("/teams/{id}")
    @Operation(summary = "Cập nhật đội bóng (Captain)")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đội thành công", teamService.updateTeam(id, request)));
    }

    /**
     * Giải tán đội (captain only)
     */
    @DeleteMapping("/teams/{id}")
    @Operation(summary = "Giải tán đội bóng (Captain)")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok(ApiResponse.success("Giải tán đội thành công", null));
    }

    /**
     * Thêm thành viên vào đội
     */
    @PostMapping("/teams/{id}/members")
    @Operation(summary = "Thêm thành viên vào đội")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberRequest request) {
        TeamMemberResponse member = teamService.addMember(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm thành viên thành công", member));
    }

    /**
     * Xóa thành viên (captain only)
     */
    @PutMapping("/teams/{id}/members/{memberId}/remove")
    @Operation(summary = "Xóa thành viên khỏi đội (Captain)")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id,
            @PathVariable Long memberId) {
        teamService.removeMember(id, memberId);
        return ResponseEntity.ok(ApiResponse.success("Xóa thành viên thành công", null));
    }

    /**
     * Chuyển quyền đội trưởng
     */
    @PutMapping("/teams/{id}/members/{memberId}/captain")
    @Operation(summary = "Chuyển quyền đội trưởng")
    public ResponseEntity<ApiResponse<TeamResponse>> transferCaptain(
            @PathVariable Long id,
            @PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success("Chuyển quyền đội trưởng thành công",
                teamService.transferCaptain(id, memberId)));
    }

    /**
     * Rời đội
     */
    @PutMapping("/teams/{id}/leave")
    @Operation(summary = "Rời đội bóng")
    public ResponseEntity<ApiResponse<Void>> leaveTeam(@PathVariable Long id) {
        teamService.leaveTeam(id);
        return ResponseEntity.ok(ApiResponse.success("Rời đội thành công", null));
    }

    /**
     * Chấp nhận lời mời vào đội
     */
    @PutMapping("/team-invites/{id}/accept")
    @Operation(summary = "Chấp nhận lời mời vào đội")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> acceptInvite(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Chấp nhận lời mời thành công",
                teamService.acceptInvite(id)));
    }

    /**
     * Từ chối lời mời vào đội
     */
    @PutMapping("/team-invites/{id}/reject")
    @Operation(summary = "Từ chối lời mời vào đội")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> rejectInvite(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Từ chối lời mời thành công",
                teamService.rejectInvite(id)));
    }
}
