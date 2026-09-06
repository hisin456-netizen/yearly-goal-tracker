package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.response.ApiResponse;
import com.hoeseok.yearly_goal_tracker.dto.ai.AiSuggestRequest;
import com.hoeseok.yearly_goal_tracker.dto.ai.SubTaskSuggestionResponse;
import com.hoeseok.yearly_goal_tracker.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/suggest-subtasks")
    public ResponseEntity<ApiResponse<List<SubTaskSuggestionResponse>>> suggestSubTasks(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AiSuggestRequest request
    ) {
        List<SubTaskSuggestionResponse> suggestions = aiService.suggestSubTasks(
                request.getGoalTitle(),
                request.getCategory(),
                request.getDescription()
        );
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }
}
