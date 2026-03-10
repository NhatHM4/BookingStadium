package com.booking.stadium.dto.booking;

import com.booking.stadium.enums.CostSharing;
import com.booking.stadium.enums.SkillLevel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Field ID không được để trống")
    private Long fieldId;

    @NotNull(message = "Time Slot ID không được để trống")
    private Long timeSlotId;

    @NotNull(message = "Ngày đặt sân không được để trống")
    private LocalDate bookingDate;

    private String note;

    private Boolean isMatchRequest = false;

    // ========== Match Request Fields (chỉ dùng khi isMatchRequest = true)
    // ==========

    // === OPTION 1: Dùng đội có sẵn hoặc tạo đội nhanh ===
    private Long teamId; // ID đội có sẵn (optional nếu createQuickTeam = true hoặc dùng option 2)

    private Boolean createQuickTeam; // true = tạo đội nhanh, false/null = dùng teamId có sẵn hoặc không cần đội

    private String quickTeamName; // Tên đội (bắt buộc nếu createQuickTeam = true)

    private SkillLevel quickTeamSkillLevel; // Trình độ đội (optional cho quick team)

    // === OPTION 2: Không cần đội - chỉ cần tên và SĐT ===
    private String hostName; // Tên người chủ kèo (bắt buộc nếu không có team)

    // === Thông tin chung ===
    private SkillLevel requiredSkillLevel; // Yêu cầu trình độ đối thủ

    private CostSharing costSharing; // Cách chia tiền sân (mặc định: WIN_LOSE 70/30)

    private BigDecimal hostSharePercent; // % tiền chủ nhà trả (dùng khi costSharing = CUSTOM)

    private BigDecimal opponentSharePercent; // % tiền đối thủ trả (dùng khi costSharing = CUSTOM)

    private String matchMessage; // Lời nhắn cho đối thủ

    private String contactPhone; // SĐT liên hệ (bắt buộc nếu không có team)
}
