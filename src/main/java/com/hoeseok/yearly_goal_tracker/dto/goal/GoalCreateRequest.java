package com.hoeseok.yearly_goal_tracker.dto.goal;

import com.hoeseok.yearly_goal_tracker.domain.enums.GoalCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalCreateRequest {

    private Long userId;

    @NotBlank(message = "목표 제목은 필수 입력값입니다.")
    @Size(max = 100, message = "목표 제목은 100자 이하이어야 합니다.")
    private String title;

    private String description;

    @NotNull(message = "카테고리는 필수 선택값입니다.")
    private GoalCategory category;

    @NotNull(message = "시작일은 필수 입력값입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수 입력값입니다.")
    private LocalDate endDate;

    @Min(value = 0, message = "목표 진척률은 0 이상이어야 합니다.")
    @Max(value = 100, message = "목표 진척률은 100 이하이어야 합니다.")
    @Builder.Default
    private Integer targetProgressRate = 100;
}
