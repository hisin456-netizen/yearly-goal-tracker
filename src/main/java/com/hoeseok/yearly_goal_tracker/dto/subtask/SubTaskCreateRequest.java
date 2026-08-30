package com.hoeseok.yearly_goal_tracker.dto.subtask;

import com.hoeseok.yearly_goal_tracker.domain.enums.PeriodType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubTaskCreateRequest {

    @NotBlank(message = "태스크 제목은 필수 입력값입니다.")
    @Size(max = 100, message = "태스크 제목은 100자 이하이어야 합니다.")
    private String title;

    @NotNull(message = "주기 유형(PeriodType)은 필수입니다.")
    @Builder.Default
    private PeriodType periodType = PeriodType.WEEKLY;

    @NotNull(message = "목표 횟수/수치는 필수입니다.")
    @Min(value = 1, message = "목표 횟수는 최소 1 이상이어야 합니다.")
    @Builder.Default
    private Integer targetCount = 1;
}
