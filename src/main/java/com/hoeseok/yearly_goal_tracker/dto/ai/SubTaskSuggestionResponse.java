package com.hoeseok.yearly_goal_tracker.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubTaskSuggestionResponse {
    private String title;
    private String periodType;
    private Integer targetCount;
    private String reason;
}
