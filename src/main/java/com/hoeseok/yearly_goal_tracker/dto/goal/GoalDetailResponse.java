package com.hoeseok.yearly_goal_tracker.dto.goal;

import com.hoeseok.yearly_goal_tracker.domain.Goal;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalCategory;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalStatus;
import com.hoeseok.yearly_goal_tracker.dto.subtask.SubTaskResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class GoalDetailResponse {

    private final Long id;
    private final Long userId;
    private final String title;
    private final String description;
    private final GoalCategory category;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final GoalStatus status;
    private final Integer targetProgressRate;
    private final List<SubTaskResponse> subTasks;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static GoalDetailResponse from(Goal goal) {
        List<SubTaskResponse> subTaskList = goal.getSubTasks() == null ? Collections.emptyList() :
                goal.getSubTasks().stream()
                        .map(SubTaskResponse::from)
                        .collect(Collectors.toList());

        return GoalDetailResponse.builder()
                .id(goal.getId())
                .userId(goal.getUser().getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .category(goal.getCategory())
                .startDate(goal.getStartDate())
                .endDate(goal.getEndDate())
                .status(goal.getStatus())
                .targetProgressRate(goal.getTargetProgressRate())
                .subTasks(subTaskList)
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
