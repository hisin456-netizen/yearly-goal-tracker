package com.hoeseok.yearly_goal_tracker.domain;

import com.hoeseok.yearly_goal_tracker.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, columnDefinition = "varchar(255) default ''")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'ROLE_USER'")
    @Builder.Default
    private UserRole role = UserRole.ROLE_USER;

    @Column(length = 255)
    private String discordWebhookUrl;

    @Column(columnDefinition = "integer default 30")
    @Builder.Default
    private Integer defaultReminderMinutes = 30;

    @Column(length = 20)
    private String provider;

    @Column(length = 100)
    private String providerId;

    public void linkProvider(String provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateRole(UserRole role) {
        this.role = role;
    }

    public void updateNotificationSettings(String discordWebhookUrl, Integer defaultReminderMinutes) {
        if (discordWebhookUrl != null) this.discordWebhookUrl = discordWebhookUrl;
        if (defaultReminderMinutes != null) this.defaultReminderMinutes = defaultReminderMinutes;
    }
}

