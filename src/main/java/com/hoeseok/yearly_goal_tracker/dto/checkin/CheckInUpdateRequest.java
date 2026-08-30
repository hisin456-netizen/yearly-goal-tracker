package com.hoeseok.yearly_goal_tracker.dto.checkin;

import com.hoeseok.yearly_goal_tracker.domain.enums.CheckInStatus;
import jakarta.validation.constraints.Max;
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
public class CheckInUpdateRequest {

    private CheckInStatus status;

    @Min(value = 0, message = "진척률은 0 이상이어야 합니다.")
    @Max(value = 100, message = "진척률은 100 이하이어야 합니다.")
    private Integer progressRate;

    @Size(max = 500, message = "메모는 500자 이하이어야 합니다.")
    private String memo;
}
