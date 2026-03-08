package com.booking.stadium.service;

import com.booking.stadium.dto.team.AddMemberRequest;
import com.booking.stadium.dto.team.TeamMemberResponse;
import com.booking.stadium.dto.team.TeamRequest;
import com.booking.stadium.dto.team.TeamResponse;
import com.booking.stadium.entity.Team;
import com.booking.stadium.entity.TeamMember;
import com.booking.stadium.entity.User;
import com.booking.stadium.enums.TeamMemberRole;
import com.booking.stadium.enums.TeamMemberStatus;
import com.booking.stadium.exception.BadRequestException;
import com.booking.stadium.exception.ResourceNotFoundException;
import com.booking.stadium.exception.UnauthorizedException;
import com.booking.stadium.repository.TeamMemberRepository;
import com.booking.stadium.repository.TeamRepository;
import com.booking.stadium.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository,
                       TeamMemberRepository teamMemberRepository,
                       UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    // ========== TEAM CRUD ==========

    /**
     * Tạo đội bóng mới - người tạo tự động thành đội trưởng
     */
    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        User captain = getCurrentUser();

        Team team = Team.builder()
                .name(request.getName())
                .logoUrl(request.getLogoUrl())
                .description(request.getDescription())
                .preferredFieldType(request.getPreferredFieldType())
                .skillLevel(request.getSkillLevel() != null ? request.getSkillLevel() : com.booking.stadium.enums.SkillLevel.INTERMEDIATE)
                .captain(captain)
                .memberCount(1)
                .city(request.getCity())
                .district(request.getDistrict())
                .build();

        team = teamRepository.save(team);

        // Tạo team member cho đội trưởng
        TeamMember captainMember = TeamMember.builder()
                .team(team)
                .user(captain)
                .role(TeamMemberRole.CAPTAIN)
                .status(TeamMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        teamMemberRepository.save(captainMember);

        return TeamResponse.fromEntity(team);
    }

    /**
     * Lấy danh sách đội của tôi (captain + member)
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getMyTeams() {
        User user = getCurrentUser();
        List<Team> teams = teamRepository.findTeamsByMemberId(user.getId());
        return teams.stream().map(TeamResponse::fromEntity).toList();
    }

    /**
     * Chi tiết đội (kèm danh sách thành viên)
     */
    @Transactional(readOnly = true)
    public TeamResponse getTeamDetail(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        List<TeamMemberResponse> members = teamMemberRepository.findByTeamId(teamId)
                .stream().map(TeamMemberResponse::fromEntity).toList();

        return TeamResponse.fromEntityWithMembers(team, members);
    }

    /**
     * Cập nhật thông tin đội - chỉ captain
     */
    @Transactional
    public TeamResponse updateTeam(Long teamId, TeamRequest request) {
        User captain = getCurrentUser();
        Team team = getTeamAsCaptain(teamId, captain);

        team.setName(request.getName());
        if (request.getLogoUrl() != null) team.setLogoUrl(request.getLogoUrl());
        if (request.getDescription() != null) team.setDescription(request.getDescription());
        if (request.getPreferredFieldType() != null) team.setPreferredFieldType(request.getPreferredFieldType());
        if (request.getSkillLevel() != null) team.setSkillLevel(request.getSkillLevel());
        if (request.getCity() != null) team.setCity(request.getCity());
        if (request.getDistrict() != null) team.setDistrict(request.getDistrict());

        team = teamRepository.save(team);
        return TeamResponse.fromEntity(team);
    }

    /**
     * Giải tán đội - chỉ captain
     */
    @Transactional
    public void deleteTeam(Long teamId) {
        User captain = getCurrentUser();
        Team team = getTeamAsCaptain(teamId, captain);

        team.setIsActive(false);
        teamRepository.save(team);

        // Cập nhật tất cả thành viên -> LEFT
        List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatus(teamId, TeamMemberStatus.ACTIVE);
        for (TeamMember member : members) {
            member.setStatus(TeamMemberStatus.LEFT);
        }
        teamMemberRepository.saveAll(members);
    }

    // ========== TEAM MEMBER MANAGEMENT ==========

    /**
     * Mời thành viên vào đội (qua email)
     */
    @Transactional
    public TeamMemberResponse addMember(Long teamId, AddMemberRequest request) {
        User captain = getCurrentUser();
        Team team = getTeamAsCaptain(teamId, captain);

        User invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (invitedUser.getId().equals(captain.getId())) {
            throw new BadRequestException("Không thể mời chính mình");
        }

        // Kiểm tra đã là thành viên chưa
        var existingOpt = teamMemberRepository.findByTeamIdAndUserId(teamId, invitedUser.getId());
        if (existingOpt.isPresent()) {
            TeamMember existing = existingOpt.get();
            if (existing.getStatus() == TeamMemberStatus.ACTIVE || existing.getStatus() == TeamMemberStatus.PENDING) {
                throw new BadRequestException("Người dùng đã là thành viên hoặc đang chờ xác nhận");
            }
            // Re-invite previously LEFT/KICKED member
            existing.setStatus(TeamMemberStatus.PENDING);
            existing.setRole(TeamMemberRole.MEMBER);
            existing = teamMemberRepository.save(existing);
            return TeamMemberResponse.fromEntity(existing);
        }

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(invitedUser)
                .role(TeamMemberRole.MEMBER)
                .status(TeamMemberStatus.PENDING)
                .build();

        member = teamMemberRepository.save(member);
        return TeamMemberResponse.fromEntity(member);
    }

    /**
     * Chấp nhận lời mời vào đội
     */
    @Transactional
    public TeamMemberResponse acceptInvite(Long memberId) {
        User user = getCurrentUser();

        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember", "id", memberId));

        if (!member.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không phải người được mời");
        }

        if (member.getStatus() != TeamMemberStatus.PENDING) {
            throw new BadRequestException("Lời mời không ở trạng thái chờ");
        }

        member.setStatus(TeamMemberStatus.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());
        teamMemberRepository.save(member);

        // Tăng member count
        Team team = member.getTeam();
        team.setMemberCount(team.getMemberCount() + 1);
        teamRepository.save(team);

        return TeamMemberResponse.fromEntity(member);
    }

    /**
     * Từ chối lời mời
     */
    @Transactional
    public TeamMemberResponse rejectInvite(Long memberId) {
        User user = getCurrentUser();

        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember", "id", memberId));

        if (!member.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không phải người được mời");
        }

        if (member.getStatus() != TeamMemberStatus.PENDING) {
            throw new BadRequestException("Lời mời không ở trạng thái chờ");
        }

        member.setStatus(TeamMemberStatus.LEFT);
        teamMemberRepository.save(member);

        return TeamMemberResponse.fromEntity(member);
    }

    /**
     * Xóa thành viên khỏi đội - chỉ captain
     */
    @Transactional
    public void removeMember(Long teamId, Long userId) {
        User captain = getCurrentUser();
        Team team = getTeamAsCaptain(teamId, captain);

        if (userId.equals(captain.getId())) {
            throw new BadRequestException("Đội trưởng không thể xóa chính mình");
        }

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember", "teamId+userId", teamId + "," + userId));

        if (member.getStatus() != TeamMemberStatus.ACTIVE) {
            throw new BadRequestException("Thành viên không ở trạng thái active");
        }

        member.setStatus(TeamMemberStatus.KICKED);
        teamMemberRepository.save(member);

        team.setMemberCount(Math.max(1, team.getMemberCount() - 1));
        teamRepository.save(team);
    }

    /**
     * Chuyển quyền đội trưởng
     */
    @Transactional
    public TeamResponse transferCaptain(Long teamId, Long newCaptainUserId) {
        User currentCaptain = getCurrentUser();
        Team team = getTeamAsCaptain(teamId, currentCaptain);

        TeamMember newCaptainMember = teamMemberRepository.findByTeamIdAndUserId(teamId, newCaptainUserId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember", "userId", newCaptainUserId));

        if (newCaptainMember.getStatus() != TeamMemberStatus.ACTIVE) {
            throw new BadRequestException("Thành viên phải ở trạng thái active");
        }

        // Chuyển captain hiện tại thành member
        TeamMember currentCaptainMember = teamMemberRepository.findByTeamIdAndUserId(teamId, currentCaptain.getId())
                .orElseThrow();
        currentCaptainMember.setRole(TeamMemberRole.MEMBER);
        teamMemberRepository.save(currentCaptainMember);

        // Chuyển thành viên mới thành captain
        newCaptainMember.setRole(TeamMemberRole.CAPTAIN);
        teamMemberRepository.save(newCaptainMember);

        // Cập nhật team
        User newCaptainUser = newCaptainMember.getUser();
        team.setCaptain(newCaptainUser);
        team = teamRepository.save(team);

        return TeamResponse.fromEntity(team);
    }

    /**
     * Rời đội - thành viên thường (không phải captain)
     */
    @Transactional
    public void leaveTeam(Long teamId) {
        User user = getCurrentUser();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        if (team.getCaptain().getId().equals(user.getId())) {
            throw new BadRequestException("Đội trưởng không thể rời đội. Hãy chuyển quyền trước hoặc giải tán đội");
        }

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, user.getId())
                .orElseThrow(() -> new BadRequestException("Bạn không phải thành viên đội này"));

        if (member.getStatus() != TeamMemberStatus.ACTIVE) {
            throw new BadRequestException("Bạn không phải thành viên active");
        }

        member.setStatus(TeamMemberStatus.LEFT);
        teamMemberRepository.save(member);

        team.setMemberCount(Math.max(1, team.getMemberCount() - 1));
        teamRepository.save(team);
    }

    // ========== HELPERS ==========

    private Team getTeamAsCaptain(Long teamId, User captain) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        if (!team.getCaptain().getId().equals(captain.getId())) {
            throw new UnauthorizedException("Bạn không phải đội trưởng của đội này");
        }

        if (!team.getIsActive()) {
            throw new BadRequestException("Đội đã bị giải tán");
        }

        return team;
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
    }
}
