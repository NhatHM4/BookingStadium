package com.booking.stadium.enums;

public enum CostSharing {
    WIN_LOSE, // 70% (thắng) / 30% (thua) - Mặc định
    EQUAL_SPLIT, // 50% / 50%
    HOST_PAY, // 100% / 0%
    OPPONENT_PAY, // 0% / 100%
    CUSTOM // Tự định nghĩa %
}
