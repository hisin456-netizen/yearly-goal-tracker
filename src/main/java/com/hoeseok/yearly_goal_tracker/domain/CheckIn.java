package com.hoeseok.yearly_goal_tracker.domain;

import com.hoeseok.yearly_goal_tracker.domain.enums.CheckInStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "check_ins", uniqueConstraints = {
        @UniqueConstraint(name = "uk_subtask_date", columnNames = {"sub_task_id", "check_in_date"})
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CheckIn extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_task_id", nullable = false)
    private SubTask subTask;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CheckInStatus status = CheckInStatus.SUCCESS;

    @Column(nullable = false)
    @Builder.Default
    private Integer progressRate = 100;

    @Column(length = 500)
    private String memo;

    public void update(CheckInStatus status, Integer progressRate, String memo) {
        if (status != null) this.status = status;
        if (progressRate != null) this.progressRate = progressRate;
        if (memo != null) this.memo = memo;
    }
}
