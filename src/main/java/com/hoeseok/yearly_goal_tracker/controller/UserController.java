package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.common.response.ApiResponse;
import com.hoeseok.yearly_goal_tracker.dto.auth.LoginHistoryResponse;
import com.hoeseok.yearly_goal_tracker.dto.user.NotificationSettingUpdateRequest;
import com.hoeseok.yearly_goal_tracker.dto.user.UserResponse;
import com.hoeseok.yearly_goal_tracker.service.LoginHistoryService;
import com.hoeseok.yearly_goal_tracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LoginHistoryService loginHistoryService;

    // 사용자 생성은 /api/v1/auth/signup 으로만, 전체 사용자 목록 조회는 제공하지 않는다.
    // 응답에 이메일/Discord 웹훅 URL이 포함되므로 프로필은 본인 것만 조회할 수 있다.
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @AuthenticationPrincipal Long authUserId,
            @PathVariable Long id) {
        if (authUserId == null || !authUserId.equals(id)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/me/notification-settings")
    public ResponseEntity<ApiResponse<UserResponse>> updateNotificationSettings(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NotificationSettingUpdateRequest request) {
        UserResponse response = userService.updateNotificationSettings(userId, request);
        return ResponseEntity.ok(ApiResponse.success("알림 설정이 저장되었습니다.", response));
    }

    @GetMapping("/me/login-history")
    public ResponseEntity<ApiResponse<List<LoginHistoryResponse>>> getMyLoginHistory(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(loginHistoryService.getRecentHistory(userId)));
    }
}
