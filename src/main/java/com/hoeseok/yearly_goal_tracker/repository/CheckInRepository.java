package com.hoeseok.yearly_goal_tracker.repository;

import com.hoeseok.yearly_goal_tracker.domain.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findBySubTaskIdOrderByCheckInDateDesc(Long subTaskId);

    Optional<CheckIn> findBySubTaskIdAndCheckInDate(Long subTaskId, LocalDate checkInDate);

    boolean existsBySubTaskIdAndCheckInDate(Long subTaskId, LocalDate checkInDate);

    List<CheckIn> findBySubTaskIdAndCheckInDateBetweenOrderByCheckInDateAsc(Long subTaskId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT c FROM CheckIn c WHERE c.subTask.goal.user.id = :userId AND c.checkInDate BETWEEN :startDate AND :endDate ORDER BY c.checkInDate ASC")
    List<CheckIn> findByUserIdAndCheckInDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT c FROM CheckIn c WHERE c.subTask.goal.user.id = :userId ORDER BY c.checkInDate DESC")
    List<CheckIn> findByUserIdOrderByCheckInDateDesc(@Param("userId") Long userId);
}
