package com.hoeseok.yearly_goal_tracker.dto.dashboard;

import com.hoeseok.yearly_goal_tracker.domain.enums.GoalCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private int totalGoals;
    private int activeGoals;
    private int completedGoals;
    private int annualProgressRate;
    private int weeklyCheckInRate;
    private int currentStreakDays;
    private Map<GoalCategory, Long> categoryCount;
}
