package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.Goal;
import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalCategory;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalStatus;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalDetailResponse;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalResponse;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalUpdateRequest;
import com.hoeseok.yearly_goal_tracker.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private GoalService goalService;

    private User user;
    private Goal goal;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .username("홍길동")
                .build();

        goal = Goal.builder()
                .id(100L)
                .user(user)
                .title("정보처리기사 취득")
                .description("2026년 정처기 동차 합격")
                .category(GoalCategory.STUDY)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status(GoalStatus.IN_PROGRESS)
                .targetProgressRate(100)
                .build();
    }

    @Test
    @DisplayName("목표 생성 성공")
    void createGoal_success() {
        // given
        GoalCreateRequest request = GoalCreateRequest.builder()
                .userId(1L)
                .title("정보처리기사 취득")
                .description("2026년 정처기 동차 합격")
                .category(GoalCategory.STUDY)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .targetProgressRate(100)
                .build();

        given(userService.findUserById(1L)).willReturn(user);
        given(goalRepository.save(any(Goal.class))).willReturn(goal);

        // when
        GoalResponse response = goalService.createGoal(request);

        // then
        assertThat(response.getTitle()).isEqualTo("정보처리기사 취득");
        assertThat(response.getCategory()).isEqualTo(GoalCategory.STUDY);
        assertThat(response.getStatus()).isEqualTo(GoalStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("목표 생성 실패 - 시작일이 종료일보다 늦음")
    void createGoal_invalidDateRange() {
        // given
        GoalCreateRequest request = GoalCreateRequest.builder()
                .userId(1L)
                .title("잘못된 목표")
                .category(GoalCategory.STUDY)
                .startDate(LocalDate.of(2026, 12, 31))
                .endDate(LocalDate.of(2026, 1, 1))
                .build();

        // when & then
        assertThatThrownBy(() -> goalService.createGoal(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_GOAL_PERIOD);
    }

    @Test
    @DisplayName("목표 상세 조회 성공")
    void getGoalDetail_success() {
        // given
        given(goalRepository.findByIdWithSubTasks(100L)).willReturn(Optional.of(goal));

        // when
        GoalDetailResponse response = goalService.getGoalDetail(100L);

        // then
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTitle()).isEqualTo("정보처리기사 취득");
    }

    @Test
    @DisplayName("목표 목록 조회 성공")
    void getGoals_success() {
        // given
        given(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(goal));

        // when
        List<GoalResponse> responses = goalService.getGoals(1L, null, null);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("정보처리기사 취득");
    }

    @Test
    @DisplayName("목표 수정 성공")
    void updateGoal_success() {
        // given
        GoalUpdateRequest request = GoalUpdateRequest.builder()
                .title("AWS SAA 취득으로 변경")
                .category(GoalCategory.CAREER)
                .build();

        given(goalRepository.findById(100L)).willReturn(Optional.of(goal));

        // when
        GoalResponse response = goalService.updateGoal(100L, request);

        // then
        assertThat(response.getTitle()).isEqualTo("AWS SAA 취득으로 변경");
        assertThat(response.getCategory()).isEqualTo(GoalCategory.CAREER);
    }

    @Test
    @DisplayName("목표 삭제 성공")
    void deleteGoal_success() {
        // given
        given(goalRepository.findById(100L)).willReturn(Optional.of(goal));

        // when
        goalService.deleteGoal(100L);

        // then
        verify(goalRepository).delete(goal);
    }
}
