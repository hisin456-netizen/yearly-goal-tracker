package com.hoeseok.yearly_goal_tracker.dto.goal;

import com.hoeseok.yearly_goal_tracker.domain.enums.GoalCategory;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalUpdateRequest {

    @Size(max = 100, message = "목표 제목은 100자 이하이어야 합니다.")
    private String title;

    private String description;

    private GoalCategory category;

    private LocalDate startDate;

    private LocalDate endDate;

    private GoalStatus status;

    @Min(value = 0, message = "목표 진척률은 0 이상이어야 합니다.")
    @Max(value = 100, message = "목표 진척률은 100 이하이어야 합니다.")
    private Integer targetProgressRate;
}
