package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.domain.Schedule;
import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleReminderScheduler {

    private final ScheduleRepository scheduleRepository;
    private final DiscordNotificationService discordNotificationService;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void sendDueReminders() {
        LocalDate today = LocalDate.now();
        List<Schedule> candidates = scheduleRepository
                .findByNotifiedFalseAndScheduleDateBetween(today.minusDays(1), today.plusDays(1));

        LocalDateTime now = LocalDateTime.now();
        for (Schedule schedule : candidates) {
            User user = schedule.getUser();
            if (!StringUtils.hasText(user.getDiscordWebhookUrl())) {
                continue;
            }

            int reminderMinutes = schedule.getReminderMinutesBefore() != null
                    ? schedule.getReminderMinutesBefore()
                    : user.getDefaultReminderMinutes();

            LocalDateTime scheduledAt = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
            LocalDateTime remindAt = scheduledAt.minusMinutes(reminderMinutes);

            if (!remindAt.isAfter(now)) {
                String content = String.format(
                        "📅 **%s** 일정이 %d분 후(%s)에 시작됩니다.%s",
                        schedule.getTitle(),
                        reminderMinutes,
                        schedule.getStartTime(),
                        StringUtils.hasText(schedule.getMemo()) ? "\n" + schedule.getMemo() : ""
                );
                discordNotificationService.sendMessage(user.getDiscordWebhookUrl(), content);
                schedule.markNotified();
            }
        }
    }
}
