package com.hoeseok.yearly_goal_tracker.dto.checkin;

import com.hoeseok.yearly_goal_tracker.domain.CheckIn;
import com.hoeseok.yearly_goal_tracker.domain.enums.CheckInStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CheckInResponse {

    private final Long id;
    private final Long subTaskId;
    private final LocalDate checkInDate;
    private final CheckInStatus status;
    private final Integer progressRate;
    private final String memo;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static CheckInResponse from(CheckIn checkIn) {
        return CheckInResponse.builder()
                .id(checkIn.getId())
                .subTaskId(checkIn.getSubTask().getId())
                .checkInDate(checkIn.getCheckInDate())
                .status(checkIn.getStatus())
                .progressRate(checkIn.getProgressRate())
                .memo(checkIn.getMemo())
                .createdAt(checkIn.getCreatedAt())
                .updatedAt(checkIn.getUpdatedAt())
                .build();
    }
}
