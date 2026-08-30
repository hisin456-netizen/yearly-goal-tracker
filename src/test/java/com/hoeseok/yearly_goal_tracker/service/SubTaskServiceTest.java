package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.domain.Goal;
import com.hoeseok.yearly_goal_tracker.domain.SubTask;
import com.hoeseok.yearly_goal_tracker.domain.enums.PeriodType;
import com.hoeseok.yearly_goal_tracker.domain.enums.TaskStatus;
import com.hoeseok.yearly_goal_tracker.dto.subtask.SubTaskCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.subtask.SubTaskResponse;
import com.hoeseok.yearly_goal_tracker.dto.subtask.SubTaskUpdateRequest;
import com.hoeseok.yearly_goal_tracker.repository.SubTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubTaskServiceTest {

    @Mock
    private SubTaskRepository subTaskRepository;

    @Mock
    private GoalService goalService;

    @InjectMocks
    private SubTaskService subTaskService;

    private Goal goal;
    private SubTask subTask;

    @BeforeEach
    void setUp() {
        goal = Goal.builder()
                .id(1L)
                .title("테스트 목표")
                .build();

        subTask = SubTask.builder()
                .id(10L)
                .goal(goal)
                .title("매주 모의고사 1회 풀기")
                .periodType(PeriodType.WEEKLY)
                .targetCount(1)
                .status(TaskStatus.IN_PROGRESS)
                .build();
    }

    @Test
    @DisplayName("하위 태스크 생성 성공")
    void createSubTask_success() {
        // given
        SubTaskCreateRequest request = SubTaskCreateRequest.builder()
                .title("매주 모의고사 1회 풀기")
                .periodType(PeriodType.WEEKLY)
                .targetCount(1)
                .build();

        given(goalService.findGoalById(1L)).willReturn(goal);
        given(subTaskRepository.save(any(SubTask.class))).willReturn(subTask);

        // when
        SubTaskResponse response = subTaskService.createSubTask(1L, request);

        // then
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("매주 모의고사 1회 풀기");
        assertThat(response.getPeriodType()).isEqualTo(PeriodType.WEEKLY);
    }

    @Test
    @DisplayName("목표별 하위 태스크 목록 조회 성공")
    void getSubTasksByGoalId_success() {
        // given
        given(subTaskRepository.findByGoalIdOrderByCreatedAtAsc(1L)).willReturn(List.of(subTask));

        // when
        List<SubTaskResponse> responses = subTaskService.getSubTasksByGoalId(1L, null);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("매주 모의고사 1회 풀기");
    }

    @Test
    @DisplayName("하위 태스크 수정 성공")
    void updateSubTask_success() {
        // given
        SubTaskUpdateRequest request = SubTaskUpdateRequest.builder()
                .title("매주 모의고사 2회 풀기로 변경")
                .targetCount(2)
                .status(TaskStatus.COMPLETED)
                .build();

        given(subTaskRepository.findById(10L)).willReturn(Optional.of(subTask));

        // when
        SubTaskResponse response = subTaskService.updateSubTask(10L, request);

        // then
        assertThat(response.getTitle()).isEqualTo("매주 모의고사 2회 풀기로 변경");
        assertThat(response.getTargetCount()).isEqualTo(2);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    @DisplayName("하위 태스크 삭제 성공")
    void deleteSubTask_success() {
        // given
        given(subTaskRepository.findById(10L)).willReturn(Optional.of(subTask));

        // when
        subTaskService.deleteSubTask(10L);

        // then
        verify(subTaskRepository).delete(subTask);
    }
}
