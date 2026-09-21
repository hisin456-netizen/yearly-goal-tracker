package com.hoeseok.yearly_goal_tracker.dto.auth;

import com.hoeseok.yearly_goal_tracker.domain.LoginHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LoginHistoryResponse {
    private Long id;
    private Boolean success;
    private String ipAddress;
    private String userAgent;
    private String failReason;
    private LocalDateTime loginAt;

    public static LoginHistoryResponse from(LoginHistory history) {
        return LoginHistoryResponse.builder()
                .id(history.getId())
                .success(history.getSuccess())
                .ipAddress(history.getIpAddress())
                .userAgent(history.getUserAgent())
                .failReason(history.getFailReason())
                .loginAt(history.getCreatedAt())
                .build();
    }
}
