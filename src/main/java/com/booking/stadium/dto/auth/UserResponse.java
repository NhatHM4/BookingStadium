package com.booking.stadium.dto.auth;

import com.booking.stadium.entity.User;
import com.booking.stadium.enums.AuthProvider;
import com.booking.stadium.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private Role role;
    private AuthProvider authProvider;
    private Boolean isActive;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .isActive(user.getIsActive())
                .build();
    }
}
