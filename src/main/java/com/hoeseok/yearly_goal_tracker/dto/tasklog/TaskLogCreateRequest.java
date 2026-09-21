package com.hoeseok.yearly_goal_tracker.dto.tasklog;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TaskLogCreateRequest {
    private String content;
    private String imageUrl;
    private Boolean isCorrect;
    private LocalDate logDate;
}
