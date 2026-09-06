package com.hoeseok.yearly_goal_tracker.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AiSuggestRequest {

    @NotBlank(message = "목표 제목은 필수입니다.")
    private String goalTitle;

    private String category;
    private String description;
}
