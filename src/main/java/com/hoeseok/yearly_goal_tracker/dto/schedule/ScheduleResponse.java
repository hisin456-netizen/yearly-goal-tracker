package com.hoeseok.yearly_goal_tracker.dto.schedule;

import com.hoeseok.yearly_goal_tracker.domain.Schedule;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class ScheduleResponse {
    private Long id;
    private String title;
    private String memo;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private Integer reminderMinutesBefore;
    private Boolean notified;

    public static ScheduleResponse from(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .memo(schedule.getMemo())
                .scheduleDate(schedule.getScheduleDate())
                .startTime(schedule.getStartTime())
                .reminderMinutesBefore(schedule.getReminderMinutesBefore())
                .notified(schedule.getNotified())
                .build();
    }
}
