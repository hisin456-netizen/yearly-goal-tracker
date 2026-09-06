package com.hoeseok.yearly_goal_tracker.dto.note;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class NoteUpdateRequest {
    private String title;
    private String content;
    private Boolean isPinned;
    private LocalDate workDate;
}
