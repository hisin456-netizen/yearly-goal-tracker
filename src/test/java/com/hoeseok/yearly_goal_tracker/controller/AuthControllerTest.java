package com.hoeseok.yearly_goal_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.common.exception.GlobalExceptionHandler;
import com.hoeseok.yearly_goal_tracker.dto.auth.LoginRequest;
import com.hoeseok.yearly_goal_tracker.dto.auth.LoginResponse;
import com.hoeseok.yearly_goal_tracker.dto.auth.SignupRequest;
import com.hoeseok.yearly_goal_tracker.dto.user.UserResponse;
import com.hoeseok.yearly_goal_tracker.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup - 회원가입 성공")
    void signup_success() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .email("test@example.com")
                .password("password123!")
                .username("홍길동")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .username("홍길동")
                .createdAt(LocalDateTime.now())
                .build();

        given(authService.signup(any(SignupRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.username").value("홍길동"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup - 이메일 중복 시 409 Conflict 반환")
    void signup_duplicateEmail() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .email("test@example.com")
                .password("password123!")
                .username("홍길동")
                .build();

        given(authService.signup(any(SignupRequest.class)))
                .willThrow(new CustomException(ErrorCode.EMAIL_DUPLICATION));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다."));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 로그인 성공 시 토큰 반환")
    void login_success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123!")
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .username("홍길동")
                .createdAt(LocalDateTime.now())
                .build();

        LoginResponse response = LoginResponse.builder()
                .token("mock-jwt-token-string")
                .user(userResponse)
                .build();

        given(authService.login(any(LoginRequest.class), any(), any())).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token-string"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 비밀번호 불일치 시 401 Unauthorized 반환")
    void login_invalidCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        given(authService.login(any(LoginRequest.class), any(), any()))
                .willThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - 내 프로필 조회 성공")
    void getMyProfile_success() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .username("홍길동")
                .createdAt(LocalDateTime.now())
                .build();

        given(authService.getMyProfile(1L)).willReturn(response);

        Authentication auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(get("/api/v1/auth/me")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }
}
