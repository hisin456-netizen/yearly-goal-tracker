package com.hoeseok.yearly_goal_tracker.dto.subtask;

import com.hoeseok.yearly_goal_tracker.domain.SubTask;
import com.hoeseok.yearly_goal_tracker.domain.enums.PeriodType;
import com.hoeseok.yearly_goal_tracker.domain.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SubTaskResponse {

    private final Long id;
    private final Long goalId;
    private final String title;
    private final PeriodType periodType;
    private final Integer targetCount;
    private final TaskStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static SubTaskResponse from(SubTask subTask) {
        return SubTaskResponse.builder()
                .id(subTask.getId())
                .goalId(subTask.getGoal().getId())
                .title(subTask.getTitle())
                .periodType(subTask.getPeriodType())
                .targetCount(subTask.getTargetCount())
                .status(subTask.getStatus())
                .createdAt(subTask.getCreatedAt())
                .updatedAt(subTask.getUpdatedAt())
                .build();
    }
}
