package com.hoeseok.yearly_goal_tracker.dto.note;

import com.hoeseok.yearly_goal_tracker.domain.enums.NoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class NoteCreateRequest {

    @NotNull(message = "노트 타입은 필수입니다.")
    private NoteType type;

    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private Long goalId;

    private Boolean isPinned;

    private LocalDate workDate;
}
