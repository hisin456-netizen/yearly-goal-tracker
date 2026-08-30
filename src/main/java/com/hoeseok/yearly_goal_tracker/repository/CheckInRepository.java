package com.hoeseok.yearly_goal_tracker.repository;

import com.hoeseok.yearly_goal_tracker.domain.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findBySubTaskIdOrderByCheckInDateDesc(Long subTaskId);

    Optional<CheckIn> findBySubTaskIdAndCheckInDate(Long subTaskId, LocalDate checkInDate);

    boolean existsBySubTaskIdAndCheckInDate(Long subTaskId, LocalDate checkInDate);

    List<CheckIn> findBySubTaskIdAndCheckInDateBetweenOrderByCheckInDateAsc(Long subTaskId, LocalDate startDate, LocalDate endDate);
}
