package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.response.ApiResponse;
import com.hoeseok.yearly_goal_tracker.dto.schedule.ScheduleCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.schedule.ScheduleResponse;
import com.hoeseok.yearly_goal_tracker.dto.schedule.ScheduleUpdateRequest;
import com.hoeseok.yearly_goal_tracker.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedules(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getSchedules(userId, targetYear, targetMonth)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getSchedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getSchedule(userId, id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ScheduleCreateRequest request) {
        ScheduleResponse response = scheduleService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("일정이 등록되었습니다.", response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("일정이 수정되었습니다.", scheduleService.update(userId, id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        scheduleService.delete(userId, id);
        return ResponseEntity.ok(ApiResponse.success("일정이 삭제되었습니다.", null));
    }
}
