package com.hoeseok.yearly_goal_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hoeseok.yearly_goal_tracker.common.exception.GlobalExceptionHandler;
import com.hoeseok.yearly_goal_tracker.domain.enums.CheckInStatus;
import com.hoeseok.yearly_goal_tracker.dto.checkin.CheckInCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.checkin.CheckInResponse;
import com.hoeseok.yearly_goal_tracker.service.CheckInService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CheckInControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CheckInService checkInService;

    @InjectMocks
    private CheckInController checkInController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(checkInController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/v1/sub-tasks/{subTaskId}/check-ins - 체크인 등록 성공")
    void createCheckIn() throws Exception {
        LocalDate today = LocalDate.of(2026, 8, 30);
        CheckInCreateRequest request = CheckInCreateRequest.builder()
                .checkInDate(today)
                .status(CheckInStatus.SUCCESS)
                .progressRate(100)
                .memo("목표 달성")
                .build();

        CheckInResponse response = CheckInResponse.builder()
                .id(1L)
                .subTaskId(10L)
                .checkInDate(today)
                .status(CheckInStatus.SUCCESS)
                .progressRate(100)
                .memo("목표 달성")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(checkInService.createCheckIn(eq(10L), any(CheckInCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/sub-tasks/10/check-ins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.memo").value("목표 달성"));
    }

    @Test
    @DisplayName("GET /api/v1/sub-tasks/{subTaskId}/check-ins - 하위 태스크별 체크인 목록 조회 성공")
    void getCheckIns() throws Exception {
        LocalDate today = LocalDate.of(2026, 8, 30);
        CheckInResponse response = CheckInResponse.builder()
                .id(1L)
                .subTaskId(10L)
                .checkInDate(today)
                .status(CheckInStatus.SUCCESS)
                .progressRate(100)
                .memo("목표 달성")
                .build();

        given(checkInService.getCheckInsBySubTaskId(10L)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/sub-tasks/10/check-ins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].memo").value("목표 달성"));
    }
}
