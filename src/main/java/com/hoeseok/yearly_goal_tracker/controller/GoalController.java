package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.response.ApiResponse;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalCategory;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalStatus;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalDetailResponse;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalResponse;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalUpdateRequest;
import com.hoeseok.yearly_goal_tracker.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            @AuthenticationPrincipal Long authUserId,
            @Valid @RequestBody GoalCreateRequest request
    ) {
        GoalResponse response = (request.getUserId() != null)
                ? goalService.createGoal(request)
                : goalService.createGoal(authUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("목표가 등록되었습니다.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoals(
            @AuthenticationPrincipal Long authUserId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) GoalCategory category,
            @RequestParam(required = false) GoalStatus status
    ) {
        Long targetUserId = userId != null ? userId : authUserId;
        List<GoalResponse> responses = goalService.getGoals(targetUserId, category, status);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalDetailResponse>> getGoalDetail(
            @AuthenticationPrincipal Long authUserId,
            @PathVariable Long id
    ) {
        GoalDetailResponse response = (authUserId != null)
                ? goalService.getGoalDetail(authUserId, id)
                : goalService.getGoalDetail(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @AuthenticationPrincipal Long authUserId,
            @PathVariable Long id,
            @Valid @RequestBody GoalUpdateRequest request
    ) {
        GoalResponse response = (authUserId != null)
                ? goalService.updateGoal(authUserId, id, request)
                : goalService.updateGoal(id, request);
        return ResponseEntity.ok(ApiResponse.success("목표가 수정되었습니다.", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoalStatus(
            @AuthenticationPrincipal Long authUserId,
            @PathVariable Long id,
            @RequestParam GoalStatus status
    ) {
        GoalResponse response = (authUserId != null)
                ? goalService.updateGoalStatus(authUserId, id, status)
                : goalService.updateGoalStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("목표 상태가 변경되었습니다.", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @AuthenticationPrincipal Long authUserId,
            @PathVariable Long id
    ) {
        if (authUserId != null) {
            goalService.deleteGoal(authUserId, id);
        } else {
            goalService.deleteGoal(id);
        }
        return ResponseEntity.ok(ApiResponse.success("목표가 삭제되었습니다.", null));
    }
}
