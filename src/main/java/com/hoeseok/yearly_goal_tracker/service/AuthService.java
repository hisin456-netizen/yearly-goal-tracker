package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.dto.auth.LoginRequest;
import com.hoeseok.yearly_goal_tracker.dto.auth.LoginResponse;
import com.hoeseok.yearly_goal_tracker.dto.auth.SignupRequest;
import com.hoeseok.yearly_goal_tracker.dto.user.UserResponse;
import com.hoeseok.yearly_goal_tracker.repository.UserRepository;
import com.hoeseok.yearly_goal_tracker.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginHistoryService loginHistoryService;
    private final SignupPolicy signupPolicy;

    /**
     * 회원가입
     */
    @Transactional
    public UserResponse signup(SignupRequest request) {
        // 허용된 이메일만 가입 가능 (중복 체크보다 먼저 검사해서 가입 여부가 노출되지 않게 한다)
        if (!signupPolicy.isAllowed(request.getEmail())) {
            throw new CustomException(ErrorCode.SIGNUP_NOT_ALLOWED);
        }

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATION);
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    /**
     * 로그인 → JWT 토큰 발급
     */
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginHistoryService.record(request.getEmail(), user, false, ipAddress, userAgent, "INVALID_CREDENTIALS");
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        loginHistoryService.record(request.getEmail(), user, true, ipAddress, userAgent, null);

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return LoginResponse.builder()
                .token(token)
                .user(UserResponse.from(user))
                .build();
    }

    /**
     * 현재 로그인한 사용자 프로필 조회
     */
    public UserResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }
}
