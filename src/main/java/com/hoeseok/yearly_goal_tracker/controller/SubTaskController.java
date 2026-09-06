package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.response.ApiResponse;
import com.hoeseok.yearly_goal_tracker.domain.enums.TaskStatus;
import com.hoeseok.yearly_goal_tracker.dto.subtask.SubTaskCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.subtask.SubTaskResponse;
import com.hoeseok.yearly_goal_tracker.dto.subtask.SubTaskUpdateRequest;
import com.hoeseok.yearly_goal_tracker.service.SubTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SubTaskController {

    private final SubTaskService subTaskService;

    @PostMapping("/goals/{goalId}/sub-tasks")
    public ResponseEntity<ApiResponse<SubTaskResponse>> createSubTask(
            @AuthenticationPrincipal Long authUserId,
            @PathVariable Long goalId,
            @Valid @RequestBody SubTaskCreateRequest request
    ) {
        SubTaskResponse response = (authUserId != null)
                ? subTaskService.createSubTask(authUserId, goalId, request)
                : subTaskService.createSubTask(goalId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("하위 태스크가 생성되었습니다.", response));
    }

    @GetMapping("/goals/{goalId}/sub-tasks")
    public ResponseEntity<ApiResponse<List<SubTaskResponse>>> getSubTasks(
            @PathVariable Long goalId,
            @RequestParam(required = false) TaskStatus status
    ) {
        List<SubTaskResponse> responses = subTaskService.getSubTasksByGoalId(goalId, status);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/sub-tasks/{id}")
    public ResponseEntity<ApiResponse<SubTaskResponse>> getSubTask(@PathVariable Long id) {
        SubTaskResponse response = subTaskService.getSubTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/sub-tasks/{id}")
    public ResponseEntity<ApiResponse<SubTaskResponse>> updateSubTask(
            @AuthenticationPrincipal Long authUserId,
            @PathVariable Long id,
            @Valid @RequestBody SubTaskUpdateRequest request
    ) {
        SubTaskResponse response = (authUserId != null)
                ? subTaskService.updateSubTask(authUserId, id, request)
                : subTaskService.updateSubTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("하위 태스크가 수정되었습니다.", response));
    }

    @DeleteMapping("/sub-tasks/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubTask(
            @AuthenticationPrincipal Long authUserId,
            @PathVariable Long id
    ) {
        if (authUserId != null) {
            subTaskService.deleteSubTask(authUserId, id);
        } else {
            subTaskService.deleteSubTask(id);
        }
        return ResponseEntity.ok(ApiResponse.success("하위 태스크가 삭제되었습니다.", null));
    }
}
