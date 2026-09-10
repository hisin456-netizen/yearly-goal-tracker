package com.hoeseok.yearly_goal_tracker.repository;

import com.hoeseok.yearly_goal_tracker.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserIdAndScheduleDateBetweenOrderByScheduleDateAscStartTimeAsc(
            Long userId, LocalDate from, LocalDate to);

    List<Schedule> findByNotifiedFalseAndScheduleDateBetween(LocalDate from, LocalDate to);
}
