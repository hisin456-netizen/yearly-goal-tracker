package com.hoeseok.yearly_goal_tracker.dto.schedule;

import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ScheduleUpdateRequest {

    @Size(max = 100, message = "일정 제목은 100자 이하이어야 합니다.")
    private String title;

    @Size(max = 500, message = "메모는 500자 이하이어야 합니다.")
    private String memo;

    private LocalDate scheduleDate;

    private LocalTime startTime;

    private Integer reminderMinutesBefore;
}
