package com.hoeseok.yearly_goal_tracker.dto.user;

import com.hoeseok.yearly_goal_tracker.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {

    private final Long id;
    private final String email;
    private final String username;
    private final String discordWebhookUrl;
    private final Integer defaultReminderMinutes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .discordWebhookUrl(user.getDiscordWebhookUrl())
                .defaultReminderMinutes(user.getDefaultReminderMinutes())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
