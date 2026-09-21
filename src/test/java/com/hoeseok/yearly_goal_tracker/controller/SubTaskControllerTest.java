package com.hoeseok.yearly_goal_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.common.exception.GlobalExceptionHandler;
import com.hoeseok.yearly_goal_tracker.domain.enums.PeriodType;
import com.hoeseok.yearly_goal_tracker.domain.enums.TaskStatus;
import com.hoeseok.yearly_goal_tracker.dto.subtask.SubTaskCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.subtask.SubTaskResponse;
import com.hoeseok.yearly_goal_tracker.service.SubTaskService;
import org.junit.jupiter.api.AfterEach;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SubTaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubTaskService subTaskService;

    @InjectMocks
    private SubTaskController subTaskController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subTaskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(AuthTestSupport.principalResolver())
                .build();
        AuthTestSupport.loginAs(1L);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() {
        AuthTestSupport.logout();
    }

    @Test
    @DisplayName("GET /api/v1/goals/{goalId}/sub-tasks - 남의 목표면 403")
    void getSubTasks_forbidden() throws Exception {
        given(subTaskService.getSubTasksByGoalId(eq(1L), eq(99L), any()))
                .willThrow(new CustomException(ErrorCode.FORBIDDEN));

        mockMvc.perform(get("/api/v1/goals/99/sub-tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/goals/{goalId}/sub-tasks - 하위 태스크 생성 성공")
    void createSubTask() throws Exception {
        SubTaskCreateRequest request = SubTaskCreateRequest.builder()
                .title("매주 1회 문제 풀기")
                .periodType(PeriodType.WEEKLY)
                .targetCount(1)
                .build();

        SubTaskResponse response = SubTaskResponse.builder()
                .id(1L)
                .goalId(10L)
                .title("매주 1회 문제 풀기")
                .periodType(PeriodType.WEEKLY)
                .targetCount(1)
                .status(TaskStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(subTaskService.createSubTask(eq(1L), eq(10L), any(SubTaskCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/goals/10/sub-tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("매주 1회 문제 풀기"));
    }

    @Test
    @DisplayName("GET /api/v1/goals/{goalId}/sub-tasks - 목표별 하위 태스크 목록 조회 성공")
    void getSubTasks() throws Exception {
        SubTaskResponse response = SubTaskResponse.builder()
                .id(1L)
                .goalId(10L)
                .title("매주 1회 문제 풀기")
                .periodType(PeriodType.WEEKLY)
                .targetCount(1)
                .status(TaskStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(subTaskService.getSubTasksByGoalId(eq(1L), eq(10L), any())).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/goals/10/sub-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }
}
