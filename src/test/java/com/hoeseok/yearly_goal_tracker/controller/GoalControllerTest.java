package com.hoeseok.yearly_goal_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hoeseok.yearly_goal_tracker.common.exception.GlobalExceptionHandler;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalCategory;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalStatus;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalDetailResponse;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalResponse;
import com.hoeseok.yearly_goal_tracker.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GoalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GoalService goalService;

    @InjectMocks
    private GoalController goalController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(goalController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/v1/goals - 목표 생성 성공")
    void createGoal() throws Exception {
        GoalCreateRequest request = GoalCreateRequest.builder()
                .userId(1L)
                .title("2026 자격증 취득")
                .description("정처기")
                .category(GoalCategory.STUDY)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .targetProgressRate(100)
                .build();

        GoalResponse response = GoalResponse.builder()
                .id(1L)
                .userId(1L)
                .title("2026 자격증 취득")
                .description("정처기")
                .category(GoalCategory.STUDY)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status(GoalStatus.IN_PROGRESS)
                .targetProgressRate(100)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(goalService.createGoal(any(GoalCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("2026 자격증 취득"))
                .andExpect(jsonPath("$.data.category").value("STUDY"));
    }

    @Test
    @DisplayName("GET /api/v1/goals - 목표 목록 조회 성공")
    void getGoals() throws Exception {
        GoalResponse response = GoalResponse.builder()
                .id(1L)
                .userId(1L)
                .title("2026 자격증 취득")
                .category(GoalCategory.STUDY)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status(GoalStatus.IN_PROGRESS)
                .targetProgressRate(100)
                .build();

        given(goalService.getGoals(eq(1L), any(), any())).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/goals")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].title").value("2026 자격증 취득"));
    }

    @Test
    @DisplayName("GET /api/v1/goals/{id} - 목표 상세 조회 성공")
    void getGoalDetail() throws Exception {
        GoalDetailResponse response = GoalDetailResponse.builder()
                .id(1L)
                .userId(1L)
                .title("2026 자격증 취득")
                .category(GoalCategory.STUDY)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status(GoalStatus.IN_PROGRESS)
                .targetProgressRate(100)
                .subTasks(Collections.emptyList())
                .build();

        given(goalService.getGoalDetail(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("2026 자격증 취득"));
    }

    @Test
    @DisplayName("DELETE /api/v1/goals/{id} - 목표 삭제 성공")
    void deleteGoal() throws Exception {
        mockMvc.perform(delete("/api/v1/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("목표가 삭제되었습니다."));
    }
}
