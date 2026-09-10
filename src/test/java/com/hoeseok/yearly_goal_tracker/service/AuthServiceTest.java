package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.domain.enums.UserRole;
import com.hoeseok.yearly_goal_tracker.dto.auth.LoginRequest;
import com.hoeseok.yearly_goal_tracker.dto.auth.LoginResponse;
import com.hoeseok.yearly_goal_tracker.dto.auth.SignupRequest;
import com.hoeseok.yearly_goal_tracker.dto.user.UserResponse;
import com.hoeseok.yearly_goal_tracker.repository.UserRepository;
import com.hoeseok.yearly_goal_tracker.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private LoginHistoryService loginHistoryService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공 - 비밀번호가 BCrypt로 암호화되어 저장된다")
    void signup_success() {
        // given
        SignupRequest request = SignupRequest.builder()
                .email("test@example.com")
                .username("테스터")
                .password("password123!")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("테스터")
                .password("encodedPassword")
                .role(UserRole.ROLE_USER)
                .build();

        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123!")).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        UserResponse response = authService.signup(request);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getUsername()).isEqualTo("테스터");
        verify(passwordEncoder).encode("password123!");
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 이메일인 경우 예외 발생")
    void signup_fail_duplicateEmail() {
        // given
        SignupRequest request = SignupRequest.builder()
                .email("duplicate@example.com")
                .username("테스터")
                .password("password123!")
                .build();

        given(userRepository.existsByEmail("duplicate@example.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_DUPLICATION);
    }

    @Test
    @DisplayName("로그인 성공 - JWT 토큰이 발급된다")
    void login_success() {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123!")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("테스터")
                .password("encodedPassword")
                .role(UserRole.ROLE_USER)
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123!", "encodedPassword")).willReturn(true);
        given(jwtTokenProvider.generateToken(1L, "test@example.com", "ROLE_USER")).willReturn("jwt.mock.token");

        // when
        LoginResponse response = authService.login(request, "127.0.0.1", "JUnit-Agent");

        // then
        assertThat(response.getToken()).isEqualTo("jwt.mock.token");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호가 일치하지 않으면 INVALID_CREDENTIALS 예외 발생")
    void login_fail_invalidPassword() {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("테스터")
                .password("encodedPassword")
                .role(UserRole.ROLE_USER)
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongpassword", "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request, "127.0.0.1", "JUnit-Agent"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }
}
