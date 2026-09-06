package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.domain.CheckIn;
import com.hoeseok.yearly_goal_tracker.domain.Goal;
import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.domain.enums.CheckInStatus;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalCategory;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalStatus;
import com.hoeseok.yearly_goal_tracker.dto.dashboard.DashboardSummaryResponse;
import com.hoeseok.yearly_goal_tracker.repository.CheckInRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private CheckInRepository checkInRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User user;
    private Goal goal1;
    private Goal goal2;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("테스터")
                .build();

        goal1 = Goal.builder()
                .id(10L)
                .user(user)
                .title("2026 자격증 취득")
                .category(GoalCategory.STUDY)
                .status(GoalStatus.IN_PROGRESS)
                .targetProgressRate(80)
                .build();

        goal2 = Goal.builder()
                .id(20L)
                .user(user)
                .title("헬스 주 3회")
                .category(GoalCategory.HEALTH)
                .status(GoalStatus.COMPLETED)
                .targetProgressRate(100)
                .build();
    }

    @Test
    @DisplayName("대시보드 KPI 요약 조회 성공 - 목표수, 진행률, 주간체크인율, 스트릭 정상 산출")
    void getSummary_success() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);

        CheckIn c1 = CheckIn.builder().checkInDate(today).status(CheckInStatus.SUCCESS).build();
        CheckIn c2 = CheckIn.builder().checkInDate(yesterday).status(CheckInStatus.SUCCESS).build();
        CheckIn c3 = CheckIn.builder().checkInDate(twoDaysAgo).status(CheckInStatus.FAIL).build();

        given(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(goal1, goal2));
        given(checkInRepository.findByUserIdAndCheckInDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(List.of(c1, c2, c3));
        given(checkInRepository.findByUserIdOrderByCheckInDateDesc(1L))
                .willReturn(List.of(c1, c2, c3));

        // when
        DashboardSummaryResponse response = dashboardService.getSummary(1L);

        // then
        assertThat(response.getTotalGoals()).isEqualTo(2);
        assertThat(response.getActiveGoals()).isEqualTo(1);
        assertThat(response.getCompletedGoals()).isEqualTo(1);
        assertThat(response.getAnnualProgressRate()).isEqualTo(90); // (80 + 100) / 2
        assertThat(response.getWeeklyCheckInRate()).isEqualTo(67); // 2 out of 3 = 66.6% -> 67%
        assertThat(response.getCurrentStreakDays()).isEqualTo(2); // today, yesterday
        assertThat(response.getCategoryCount().get(GoalCategory.STUDY)).isEqualTo(1L);
        assertThat(response.getCategoryCount().get(GoalCategory.HEALTH)).isEqualTo(1L);
    }

    @Test
    @DisplayName("사용자 ID가 null인 경우 빈 대시보드 요약 반환")
    void getSummary_nullUser() {
        DashboardSummaryResponse response = dashboardService.getSummary(null);

        assertThat(response.getTotalGoals()).isEqualTo(0);
        assertThat(response.getActiveGoals()).isEqualTo(0);
        assertThat(response.getAnnualProgressRate()).isEqualTo(0);
        assertThat(response.getCurrentStreakDays()).isEqualTo(0);
    }

    @Test
    @DisplayName("일일 실천 잔디 (히트맵) 조회 성공 - 포인트 일수 및 레벨 산출 정상 확인")
    void getHeatmap_success() {
        // given
        LocalDate today = LocalDate.now();
        CheckIn c1 = CheckIn.builder().checkInDate(today).status(CheckInStatus.SUCCESS).build();
        CheckIn c2 = CheckIn.builder().checkInDate(today).status(CheckInStatus.SUCCESS).build();

        given(checkInRepository.findByUserIdAndCheckInDateBetween(eq(1L), any(LocalDate.class), eq(today)))
                .willReturn(List.of(c1, c2));

        // when
        com.hoeseok.yearly_goal_tracker.dto.dashboard.HeatmapResponse response = dashboardService.getHeatmap(1L, 14);

        // then
        assertThat(response.getTotalContributions()).isEqualTo(2);
        assertThat(response.getPoints()).hasSize(14);
        com.hoeseok.yearly_goal_tracker.dto.dashboard.HeatmapResponse.Point todayPoint = response.getPoints().get(response.getPoints().size() - 1);
        assertThat(todayPoint.getDate()).isEqualTo(today);
        assertThat(todayPoint.getCount()).isEqualTo(2);
        assertThat(todayPoint.getLevel()).isEqualTo(2);
    }

    @Test
    @DisplayName("사용자 ID가 null인 경우 빈 히트맵 반환")
    void getHeatmap_nullUser() {
        com.hoeseok.yearly_goal_tracker.dto.dashboard.HeatmapResponse response = dashboardService.getHeatmap(null, 14);

        assertThat(response.getTotalContributions()).isEqualTo(0);
        assertThat(response.getPoints()).isEmpty();
    }
}
