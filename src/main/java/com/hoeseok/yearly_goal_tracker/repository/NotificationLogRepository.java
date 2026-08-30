package com.hoeseok.yearly_goal_tracker.repository;

import com.hoeseok.yearly_goal_tracker.domain.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByUserIdOrderBySentAtDesc(Long userId);

    List<NotificationLog> findByGoalIdOrderBySentAtDesc(Long goalId);
}
