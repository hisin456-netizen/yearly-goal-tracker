package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.response.ApiResponse;
import com.hoeseok.yearly_goal_tracker.dto.dashboard.DashboardSummaryResponse;
import com.hoeseok.yearly_goal_tracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @AuthenticationPrincipal Long authUserId,
            @RequestParam(required = false) Long userId
    ) {
        Long targetUserId = userId != null ? userId : authUserId;
        DashboardSummaryResponse response = dashboardService.getSummary(targetUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/heatmap")
    public ResponseEntity<ApiResponse<com.hoeseok.yearly_goal_tracker.dto.dashboard.HeatmapResponse>> getHeatmap(
            @AuthenticationPrincipal Long authUserId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "105") int days
    ) {
        Long targetUserId = userId != null ? userId : authUserId;
        com.hoeseok.yearly_goal_tracker.dto.dashboard.HeatmapResponse response = dashboardService.getHeatmap(targetUserId, days);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
