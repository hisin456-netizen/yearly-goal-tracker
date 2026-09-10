package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.response.ApiResponse;
import com.hoeseok.yearly_goal_tracker.dto.tasklog.TaskLogCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.tasklog.TaskLogResponse;
import com.hoeseok.yearly_goal_tracker.service.TaskLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TaskLogController {

    private final TaskLogService taskLogService;

    @GetMapping("/sub-tasks/{subTaskId}/logs")
    public ResponseEntity<ApiResponse<List<TaskLogResponse>>> getLogs(
            @PathVariable Long subTaskId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(taskLogService.getLogs(subTaskId, userId)));
    }

    @PostMapping("/sub-tasks/{subTaskId}/logs")
    public ResponseEntity<ApiResponse<TaskLogResponse>> create(
            @PathVariable Long subTaskId,
            @RequestBody TaskLogCreateRequest req,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(taskLogService.create(subTaskId, req, userId)));
    }

    @DeleteMapping("/task-logs/{logId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long logId,
            @AuthenticationPrincipal Long userId) {
        taskLogService.delete(logId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
