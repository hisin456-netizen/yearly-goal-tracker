package com.hoeseok.yearly_goal_tracker.domain;

import com.hoeseok.yearly_goal_tracker.domain.enums.NotificationChannel;
import com.hoeseok.yearly_goal_tracker.domain.enums.RuleType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_rules")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NotificationRule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RuleType ruleType;

    @Column(length = 255)
    private String conditionValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private NotificationChannel channel = NotificationChannel.EMAIL;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public void update(RuleType ruleType, String conditionValue, NotificationChannel channel, Boolean isActive) {
        if (ruleType != null) this.ruleType = ruleType;
        if (conditionValue != null) this.conditionValue = conditionValue;
        if (channel != null) this.channel = channel;
        if (isActive != null) this.isActive = isActive;
    }

    public void toggleActive() {
        this.isActive = !this.isActive;
    }
}
