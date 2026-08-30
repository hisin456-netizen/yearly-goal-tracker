package com.hoeseok.yearly_goal_tracker.dto.goal;

import com.hoeseok.yearly_goal_tracker.domain.Goal;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalCategory;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class GoalResponse {

    private final Long id;
    private final Long userId;
    private final String title;
    private final String description;
    private final GoalCategory category;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final GoalStatus status;
    private final Integer targetProgressRate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static GoalResponse from(Goal goal) {
        return GoalResponse.builder()
                .id(goal.getId())
                .userId(goal.getUser().getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .category(goal.getCategory())
                .startDate(goal.getStartDate())
                .endDate(goal.getEndDate())
                .status(goal.getStatus())
                .targetProgressRate(goal.getTargetProgressRate())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
