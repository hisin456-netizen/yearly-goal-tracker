package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.response.ApiResponse;
import com.hoeseok.yearly_goal_tracker.dto.auth.LoginHistoryResponse;
import com.hoeseok.yearly_goal_tracker.dto.user.NotificationSettingUpdateRequest;
import com.hoeseok.yearly_goal_tracker.dto.user.UserCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.user.UserResponse;
import com.hoeseok.yearly_goal_tracker.service.LoginHistoryService;
import com.hoeseok.yearly_goal_tracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("사용자가 성공적으로 생성되었습니다.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> responses = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
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
