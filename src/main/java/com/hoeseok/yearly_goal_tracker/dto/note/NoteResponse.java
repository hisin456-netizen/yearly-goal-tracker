package com.hoeseok.yearly_goal_tracker.dto.note;

import com.hoeseok.yearly_goal_tracker.domain.Note;
import com.hoeseok.yearly_goal_tracker.domain.enums.NoteType;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class NoteResponse {

    private final Long id;
    private final Long userId;
    private final Long goalId;
    private final String goalTitle;
    private final NoteType type;
    private final String title;
    private final String content;
    private final Boolean isPinned;
    private final LocalDate workDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public NoteResponse(Note note) {
        this.id = note.getId();
        this.userId = note.getUser().getId();
        this.goalId = note.getGoal() != null ? note.getGoal().getId() : null;
        this.goalTitle = note.getGoal() != null ? note.getGoal().getTitle() : null;
        this.type = note.getType();
        this.title = note.getTitle();
        this.content = note.getContent();
        this.isPinned = note.getIsPinned();
        this.workDate = note.getWorkDate();
        this.createdAt = note.getCreatedAt();
        this.updatedAt = note.getUpdatedAt();
    }
}
