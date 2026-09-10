package com.hoeseok.yearly_goal_tracker.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Schedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String memo;

    @Column(nullable = false)
    private LocalDate scheduleDate;

    @Column(nullable = false)
    private LocalTime startTime;

    private Integer reminderMinutesBefore;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notified = false;

    public void update(String title, String memo, LocalDate scheduleDate, LocalTime startTime, Integer reminderMinutesBefore) {
        if (title != null) this.title = title;
        if (memo != null) this.memo = memo;
        if (scheduleDate != null) this.scheduleDate = scheduleDate;
        if (startTime != null) this.startTime = startTime;
        this.reminderMinutesBefore = reminderMinutesBefore;
        this.notified = false;
    }

    public void markNotified() {
        this.notified = true;
    }
}
