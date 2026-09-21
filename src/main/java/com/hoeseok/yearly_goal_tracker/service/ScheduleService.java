package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.Schedule;
import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.dto.schedule.ScheduleCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.schedule.ScheduleResponse;
import com.hoeseok.yearly_goal_tracker.dto.schedule.ScheduleUpdateRequest;
import com.hoeseok.yearly_goal_tracker.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserService userService;

    public List<ScheduleResponse> getSchedules(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        return scheduleRepository
                .findByUserIdAndScheduleDateBetweenOrderByScheduleDateAscStartTimeAsc(userId, from, to)
                .stream().map(ScheduleResponse::from).toList();
    }

    public ScheduleResponse getSchedule(Long userId, Long scheduleId) {
        Schedule schedule = findOwnedSchedule(userId, scheduleId);
        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public ScheduleResponse create(Long userId, ScheduleCreateRequest request) {
        User user = userService.findUserById(userId);

        Schedule schedule = Schedule.builder()
                .user(user)
                .title(request.getTitle())
                .memo(request.getMemo())
                .scheduleDate(request.getScheduleDate())
                .startTime(request.getStartTime())
                .reminderMinutesBefore(request.getReminderMinutesBefore())
                .build();

        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    @Transactional
    public ScheduleResponse update(Long userId, Long scheduleId, ScheduleUpdateRequest request) {
        Schedule schedule = findOwnedSchedule(userId, scheduleId);
        schedule.update(
                request.getTitle(),
                request.getMemo(),
                request.getScheduleDate(),
                request.getStartTime(),
                request.getReminderMinutesBefore()
        );
        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public void delete(Long userId, Long scheduleId) {
        Schedule schedule = findOwnedSchedule(userId, scheduleId);
        scheduleRepository.delete(schedule);
    }

    private Schedule findOwnedSchedule(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (!schedule.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return schedule;
    }
}
