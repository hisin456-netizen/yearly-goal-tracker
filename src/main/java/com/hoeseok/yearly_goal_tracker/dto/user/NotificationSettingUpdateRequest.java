package com.hoeseok.yearly_goal_tracker.dto.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class NotificationSettingUpdateRequest {

    @Pattern(regexp = "^https://discord(app)?\\.com/api/webhooks/.+", message = "올바른 Discord Webhook URL이 아닙니다.")
    private String discordWebhookUrl;

    @Min(value = 0, message = "리마인드 시간은 0분 이상이어야 합니다.")
    private Integer defaultReminderMinutes;
}
