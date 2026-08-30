package com.hoeseok.yearly_goal_tracker.repository;

import com.hoeseok.yearly_goal_tracker.domain.SubTask;
import com.hoeseok.yearly_goal_tracker.domain.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubTaskRepository extends JpaRepository<SubTask, Long> {

    List<SubTask> findByGoalIdOrderByCreatedAtAsc(Long goalId);

    List<SubTask> findByGoalIdAndStatusOrderByCreatedAtAsc(Long goalId, TaskStatus status);

    @Query("SELECT s FROM SubTask s LEFT JOIN FETCH s.checkIns WHERE s.id = :id")
    Optional<SubTask> findByIdWithCheckIns(@Param("id") Long id);
}
