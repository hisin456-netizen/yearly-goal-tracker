package com.hoeseok.yearly_goal_tracker.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapResponse {

    private int totalContributions;
    private List<Point> points;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {
        private LocalDate date;
        private int count;
        private int level; // 0 ~ 4
    }
}
