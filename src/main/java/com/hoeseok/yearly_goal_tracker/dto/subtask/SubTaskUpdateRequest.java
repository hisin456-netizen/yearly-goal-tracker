package com.hoeseok.yearly_goal_tracker.dto.subtask;

import com.hoeseok.yearly_goal_tracker.domain.enums.PeriodType;
import com.hoeseok.yearly_goal_tracker.domain.enums.TaskStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubTaskUpdateRequest {

    @Size(max = 100, message = "태스크 제목은 100자 이하이어야 합니다.")
    private String title;

    private PeriodType periodType;

    @Min(value = 1, message = "목표 횟수는 최소 1 이상이어야 합니다.")
    private Integer targetCount;

    private TaskStatus status;
}
