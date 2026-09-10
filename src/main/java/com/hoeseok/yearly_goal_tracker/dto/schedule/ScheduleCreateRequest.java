package com.hoeseok.yearly_goal_tracker.dto.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ScheduleCreateRequest {

    @NotBlank(message = "일정 제목은 필수 입력값입니다.")
    @Size(max = 100, message = "일정 제목은 100자 이하이어야 합니다.")
    private String title;

    @Size(max = 500, message = "메모는 500자 이하이어야 합니다.")
    private String memo;

    @NotNull(message = "날짜는 필수 입력값입니다.")
    private LocalDate scheduleDate;

    @NotNull(message = "시간은 필수 입력값입니다.")
    private LocalTime startTime;

    private Integer reminderMinutesBefore;
}
