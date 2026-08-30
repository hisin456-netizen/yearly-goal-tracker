package com.hoeseok.yearly_goal_tracker.repository;

import com.hoeseok.yearly_goal_tracker.domain.Goal;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalCategory;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Goal> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, GoalStatus status);

    List<Goal> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, GoalCategory category);

    @Query("SELECT g FROM Goal g LEFT JOIN FETCH g.subTasks WHERE g.id = :id")
    Optional<Goal> findByIdWithSubTasks(@Param("id") Long id);
}
