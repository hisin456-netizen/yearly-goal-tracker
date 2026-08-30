package com.hoeseok.yearly_goal_tracker.domain;

import com.hoeseok.yearly_goal_tracker.domain.enums.PeriodType;
import com.hoeseok.yearly_goal_tracker.domain.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sub_tasks")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SubTask extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PeriodType periodType = PeriodType.WEEKLY;

    @Column(nullable = false)
    @Builder.Default
    private Integer targetCount = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TaskStatus status = TaskStatus.IN_PROGRESS;

    @Builder.Default
    @OneToMany(mappedBy = "subTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CheckIn> checkIns = new ArrayList<>();

    public void update(String title, PeriodType periodType, Integer targetCount, TaskStatus status) {
        if (title != null) this.title = title;
        if (periodType != null) this.periodType = periodType;
        if (targetCount != null) this.targetCount = targetCount;
        if (status != null) this.status = status;
    }

    public void updateStatus(TaskStatus status) {
        this.status = status;
    }
}
