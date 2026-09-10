package com.hoeseok.yearly_goal_tracker.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "task_logs")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TaskLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_task_id", nullable = false)
    private SubTask subTask;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String imageUrl;

    private Boolean isCorrect;

    private LocalDate logDate;

    public void update(String content, Boolean isCorrect, LocalDate logDate) {
        if (content != null) this.content = content;
        if (isCorrect != null) this.isCorrect = isCorrect;
        if (logDate != null) this.logDate = logDate;
    }
}
