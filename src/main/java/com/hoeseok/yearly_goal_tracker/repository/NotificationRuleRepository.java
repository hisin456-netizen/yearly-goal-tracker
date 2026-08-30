package com.hoeseok.yearly_goal_tracker.repository;

import com.hoeseok.yearly_goal_tracker.domain.NotificationRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRuleRepository extends JpaRepository<NotificationRule, Long> {

    List<NotificationRule> findByUserId(Long userId);

    List<NotificationRule> findByUserIdAndIsActiveTrue(Long userId);

    List<NotificationRule> findByGoalId(Long goalId);
}
