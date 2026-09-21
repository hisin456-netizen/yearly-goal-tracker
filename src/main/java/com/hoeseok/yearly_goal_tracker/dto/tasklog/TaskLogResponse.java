package com.hoeseok.yearly_goal_tracker.dto.tasklog;

import com.hoeseok.yearly_goal_tracker.domain.TaskLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TaskLogResponse {
    private Long id;
    private Long subTaskId;
    private String content;
    private String imageUrl;
    private Boolean isCorrect;
    private LocalDate logDate;
    private LocalDateTime createdAt;

    public static TaskLogResponse from(TaskLog log) {
        return TaskLogResponse.builder()
                .id(log.getId())
                .subTaskId(log.getSubTask().getId())
                .content(log.getContent())
                .imageUrl(log.getImageUrl())
                .isCorrect(log.getIsCorrect())
                .logDate(log.getLogDate())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
