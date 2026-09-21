package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.exception.GlobalExceptionHandler;
import com.hoeseok.yearly_goal_tracker.dto.dashboard.DashboardSummaryResponse;
import com.hoeseok.yearly_goal_tracker.service.DashboardService;
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

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
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
    @DisplayName("GET /api/v1/dashboard/summary - userId 파라미터로 다른 사용자를 지정해도 본인 통계만 조회")
    void getSummary_ignoresUserIdParam() throws Exception {
        given(dashboardService.getSummary(1L)).willReturn(DashboardSummaryResponse.builder()
                .categoryCount(Collections.emptyMap())
                .build());

        mockMvc.perform(get("/api/v1/dashboard/summary").param("userId", "2"))
                .andExpect(status().isOk());

        verify(dashboardService).getSummary(1L);
        verify(dashboardService, never()).getSummary(2L);
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/summary - 대시보드 통계 요약 조회 성공")
    void getSummary_success() throws Exception {
        DashboardSummaryResponse response = DashboardSummaryResponse.builder()
                .totalGoals(3)
                .activeGoals(2)
                .completedGoals(1)
                .annualProgressRate(75)
                .weeklyCheckInRate(85)
                .currentStreakDays(5)
                .categoryCount(Collections.emptyMap())
                .build();

        given(dashboardService.getSummary(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalGoals").value(3))
                .andExpect(jsonPath("$.data.activeGoals").value(2))
                .andExpect(jsonPath("$.data.annualProgressRate").value(75))
                .andExpect(jsonPath("$.data.weeklyCheckInRate").value(85))
                .andExpect(jsonPath("$.data.currentStreakDays").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/heatmap - 일일 실천 잔디 히트맵 조회 성공")
    void getHeatmap_success() throws Exception {
        com.hoeseok.yearly_goal_tracker.dto.dashboard.HeatmapResponse response =
                com.hoeseok.yearly_goal_tracker.dto.dashboard.HeatmapResponse.builder()
                        .totalContributions(7)
                        .points(java.util.List.of(
                                com.hoeseok.yearly_goal_tracker.dto.dashboard.HeatmapResponse.Point.builder()
                                        .date(java.time.LocalDate.now())
                                        .count(2)
                                        .level(2)
                                        .build()
                        ))
                        .build();

        given(dashboardService.getHeatmap(1L, 105)).willReturn(response);

        mockMvc.perform(get("/api/v1/dashboard/heatmap")
                        .param("days", "105"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalContributions").value(7))
                .andExpect(jsonPath("$.data.points[0].count").value(2))
                .andExpect(jsonPath("$.data.points[0].level").value(2));
    }
}
