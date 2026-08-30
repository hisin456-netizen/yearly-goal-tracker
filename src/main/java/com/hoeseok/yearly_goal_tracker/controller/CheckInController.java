package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.response.ApiResponse;
import com.hoeseok.yearly_goal_tracker.dto.checkin.CheckInCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.checkin.CheckInResponse;
import com.hoeseok.yearly_goal_tracker.dto.checkin.CheckInUpdateRequest;
import com.hoeseok.yearly_goal_tracker.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping("/sub-tasks/{subTaskId}/check-ins")
    public ResponseEntity<ApiResponse<CheckInResponse>> createCheckIn(
            @PathVariable Long subTaskId,
            @Valid @RequestBody CheckInCreateRequest request
    ) {
        CheckInResponse response = checkInService.createCheckIn(subTaskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("체크인이 기록되었습니다.", response));
    }

    @GetMapping("/sub-tasks/{subTaskId}/check-ins")
    public ResponseEntity<ApiResponse<List<CheckInResponse>>> getCheckIns(
            @PathVariable Long subTaskId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<CheckInResponse> responses;
        if (startDate != null && endDate != null) {
            responses = checkInService.getCheckInsByDateRange(subTaskId, startDate, endDate);
        } else {
            responses = checkInService.getCheckInsBySubTaskId(subTaskId);
        }
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/check-ins/{id}")
    public ResponseEntity<ApiResponse<CheckInResponse>> getCheckIn(@PathVariable Long id) {
        CheckInResponse response = checkInService.getCheckInById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/check-ins/{id}")
    public ResponseEntity<ApiResponse<CheckInResponse>> updateCheckIn(
            @PathVariable Long id,
            @Valid @RequestBody CheckInUpdateRequest request
    ) {
        CheckInResponse response = checkInService.updateCheckIn(id, request);
        return ResponseEntity.ok(ApiResponse.success("체크인 기록이 수정되었습니다.", response));
    }

    @DeleteMapping("/check-ins/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCheckIn(@PathVariable Long id) {
        checkInService.deleteCheckIn(id);
        return ResponseEntity.ok(ApiResponse.success("체크인 기록이 삭제되었습니다.", null));
    }
}
