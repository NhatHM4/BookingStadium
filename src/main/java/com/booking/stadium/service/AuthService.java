package com.booking.stadium.service;

import com.booking.stadium.dto.auth.*;
import com.booking.stadium.entity.RefreshToken;
import com.booking.stadium.entity.User;
import com.booking.stadium.enums.AuthProvider;
import com.booking.stadium.enums.Role;
import com.booking.stadium.exception.BadRequestException;
import com.booking.stadium.exception.ResourceNotFoundException;
import com.booking.stadium.repository.UserRepository;
import com.booking.stadium.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.social.default-password}")
    private String socialDefaultPassword;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public JwtResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng");
        }

        // Check if phone already exists
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Số điện thoại đã được sử dụng");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole())
                .isActive(true)
                .build();

        userRepository.save(user);

        // Generate access token + refresh token
        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new JwtResponse(accessToken, refreshToken.getToken(), UserResponse.fromEntity(user));
    }

    @Transactional
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new JwtResponse(accessToken, refreshToken.getToken(), UserResponse.fromEntity(user));
    }

    @Transactional
    public JwtResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());

        User user = refreshToken.getUser();

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateToken(user.getEmail());

        // Generate new refresh token (rotate)
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return new JwtResponse(newAccessToken, newRefreshToken.getToken(), UserResponse.fromEntity(user));
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        refreshTokenService.revokeByUser(refreshToken.getUser());
    }

    public UserResponse getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public JwtResponse socialLogin(SocialLoginRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

        User user;
        if (existingUser.isPresent()) {
            // User đã tồn tại — cập nhật authProvider nếu đang là LOCAL
            user = existingUser.get();
            if (user.getAuthProvider() == AuthProvider.LOCAL) {
                user.setAuthProvider(AuthProvider.SOCIAL);
                userRepository.save(user);
            }
        } else {
            // Tạo user mới với default password
            user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(socialDefaultPassword))
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .avatarUrl(request.getAvatarUrl())
                    .role(Role.CUSTOMER)
                    .authProvider(AuthProvider.SOCIAL)
                    .isActive(true)
                    .build();
            userRepository.save(user);
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new JwtResponse(accessToken, refreshToken.getToken(), UserResponse.fromEntity(user));
    }
}
