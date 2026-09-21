package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.exception.GlobalExceptionHandler;
import com.hoeseok.yearly_goal_tracker.dto.user.UserResponse;
import com.hoeseok.yearly_goal_tracker.service.LoginHistoryService;
import com.hoeseok.yearly_goal_tracker.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private LoginHistoryService loginHistoryService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(AuthTestSupport.principalResolver())
                .build();
        AuthTestSupport.loginAs(1L);
    }

    @AfterEach
    void tearDown() {
        AuthTestSupport.logout();
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - 본인 프로필은 조회 가능")
    void getUser_self() throws Exception {
        given(userService.getUserById(1L)).willReturn(UserResponse.builder().id(1L).email("me@test.com").username("me").build());

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("me@test.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - 다른 사용자 프로필은 403 (이메일/Discord 웹훅 URL 보호)")
    void getUser_otherUserForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/2"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getUserById(2L);
    }
}
