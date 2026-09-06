package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.domain.CheckIn;
import com.hoeseok.yearly_goal_tracker.domain.Goal;
import com.hoeseok.yearly_goal_tracker.domain.enums.CheckInStatus;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalCategory;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalStatus;
import com.hoeseok.yearly_goal_tracker.dto.dashboard.DashboardSummaryResponse;
import com.hoeseok.yearly_goal_tracker.repository.CheckInRepository;
import com.hoeseok.yearly_goal_tracker.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final GoalRepository goalRepository;
    private final CheckInRepository checkInRepository;

    public DashboardSummaryResponse getSummary(Long userId) {
        if (userId == null) {
            return DashboardSummaryResponse.builder()
                    .totalGoals(0)
                    .activeGoals(0)
                    .completedGoals(0)
                    .annualProgressRate(0)
                    .weeklyCheckInRate(0)
                    .currentStreakDays(0)
                    .categoryCount(Collections.emptyMap())
                    .build();
        }

        List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);

        int totalGoals = goals.size();
        int activeGoals = (int) goals.stream()
                .filter(g -> g.getStatus() == GoalStatus.IN_PROGRESS)
                .count();
        int completedGoals = (int) goals.stream()
                .filter(g -> g.getStatus() == GoalStatus.COMPLETED)
                .count();

        // 전체 연간 목표 평균 달성률
        int annualProgressRate = 0;
        if (totalGoals > 0) {
            int sum = goals.stream()
                    .mapToInt(g -> g.getTargetProgressRate() != null ? g.getTargetProgressRate() : 0)
                    .sum();
            annualProgressRate = Math.round((float) sum / totalGoals);
        }

        // 카테고리별 목표 수 집계
        Map<GoalCategory, Long> categoryCount = goals.stream()
                .collect(Collectors.groupingBy(Goal::getCategory, Collectors.counting()));

        // 최근 7일(오늘 포함) 주간 체크인율
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);
        List<CheckIn> weeklyCheckIns = checkInRepository.findByUserIdAndCheckInDateBetween(userId, weekAgo, today);

        int weeklyCheckInRate = 0;
        if (!weeklyCheckIns.isEmpty()) {
            long successCount = weeklyCheckIns.stream()
                    .filter(c -> c.getStatus() == CheckInStatus.SUCCESS)
                    .count();
            weeklyCheckInRate = Math.round(((float) successCount / weeklyCheckIns.size()) * 100);
        }

        // 연속 실천 스트릭 (오늘 또는 어제부터 과거로 연속된 SUCCESS 날짜 수)
        List<CheckIn> allCheckIns = checkInRepository.findByUserIdOrderByCheckInDateDesc(userId);
        Set<LocalDate> successfulDates = allCheckIns.stream()
                .filter(c -> c.getStatus() == CheckInStatus.SUCCESS)
                .map(CheckIn::getCheckInDate)
                .collect(Collectors.toSet());

        int currentStreakDays = calculateStreak(successfulDates, today);

        return DashboardSummaryResponse.builder()
                .totalGoals(totalGoals)
                .activeGoals(activeGoals)
                .completedGoals(completedGoals)
                .annualProgressRate(annualProgressRate)
                .weeklyCheckInRate(weeklyCheckInRate)
                .currentStreakDays(currentStreakDays)
                .categoryCount(categoryCount)
                .build();
    }

    private int calculateStreak(Set<LocalDate> successfulDates, LocalDate today) {
        if (successfulDates.isEmpty()) {
            return 0;
        }

        // 오늘 이미 성공했으면 오늘부터, 아니면 어제부터 연속성 확인
        LocalDate checkDate = today;
        if (!successfulDates.contains(checkDate)) {
            checkDate = today.minusDays(1);
        }

        int streak = 0;
        while (successfulDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    /**
     * 일일 실천 잔디 (Activity Heatmap) 데이터 생성
     * @param userId 사용자 ID
     * @param days 최근 일수 (기본 105일 = 15주)
     */
    public com.hoeseok.yearly_goal_tracker.dto.dashboard.HeatmapResponse getHeatmap(Long userId, int days) {
        if (userId == null) {
            return com.hoeseok.yearly_goal_tracker.dto.dashboard.HeatmapResponse.builder()
                    .totalContributions(0)
                    .points(Collections.emptyList())
                    .build();
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(Math.max(days - 1, 0));

        List<CheckIn> checkIns = checkInRepository.findByUserIdAndCheckInDateBetween(userId, startDate, today);

        Map<LocalDate, Long> countsByDate = checkIns.stream()
                .filter(c -> c.getStatus() == CheckInStatus.SUCCESS)
                .collect(Collectors.groupingBy(CheckIn::getCheckInDate, Collectors.counting()));

        List<com.hoeseok.yearly_goal_tracker.dto.dashboard.HeatmapResponse.Point> points = new java.util.ArrayList<>();
        int totalContributions = 0;

        LocalDate cur = startDate;
        while (!cur.isAfter(today)) {
            int count = countsByDate.getOrDefault(cur, 0L).intValue();
            totalContributions += count;

            int level = 0;
            if (count == 1) level = 1;
            else if (count == 2) level = 2;
            else if (count == 3) level = 3;
            else if (count >= 4) level = 4;

            points.add(com.hoeseok.yearly_goal_tracker.dto.dashboard.HeatmapResponse.Point.builder()
                    .date(cur)
                    .count(count)
                    .level(level)
                    .build());

            cur = cur.plusDays(1);
        }

        return com.hoeseok.yearly_goal_tracker.dto.dashboard.HeatmapResponse.builder()
                .totalContributions(totalContributions)
                .points(points)
                .build();
    }
}
