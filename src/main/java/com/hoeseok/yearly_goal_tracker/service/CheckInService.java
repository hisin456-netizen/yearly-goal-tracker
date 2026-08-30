package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.CheckIn;
import com.hoeseok.yearly_goal_tracker.domain.SubTask;
import com.hoeseok.yearly_goal_tracker.dto.checkin.CheckInCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.checkin.CheckInResponse;
import com.hoeseok.yearly_goal_tracker.dto.checkin.CheckInUpdateRequest;
import com.hoeseok.yearly_goal_tracker.repository.CheckInRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final SubTaskService subTaskService;

    @Transactional
    public CheckInResponse createCheckIn(Long subTaskId, CheckInCreateRequest request) {
        SubTask subTask = subTaskService.findSubTaskById(subTaskId);

        if (checkInRepository.existsBySubTaskIdAndCheckInDate(subTaskId, request.getCheckInDate())) {
            throw new CustomException(ErrorCode.CHECK_IN_ALREADY_EXISTS);
        }

        CheckIn checkIn = CheckIn.builder()
                .subTask(subTask)
                .checkInDate(request.getCheckInDate())
                .status(request.getStatus())
                .progressRate(request.getProgressRate() != null ? request.getProgressRate() : 100)
                .memo(request.getMemo())
                .build();

        CheckIn saved = checkInRepository.save(checkIn);
        return CheckInResponse.from(saved);
    }

    public List<CheckInResponse> getCheckInsBySubTaskId(Long subTaskId) {
        return checkInRepository.findBySubTaskIdOrderByCheckInDateDesc(subTaskId).stream()
                .map(CheckInResponse::from)
                .collect(Collectors.toList());
    }

    public List<CheckInResponse> getCheckInsByDateRange(Long subTaskId, LocalDate startDate, LocalDate endDate) {
        return checkInRepository.findBySubTaskIdAndCheckInDateBetweenOrderByCheckInDateAsc(subTaskId, startDate, endDate).stream()
                .map(CheckInResponse::from)
                .collect(Collectors.toList());
    }

    public CheckInResponse getCheckInById(Long checkInId) {
        CheckIn checkIn = findCheckInById(checkInId);
        return CheckInResponse.from(checkIn);
    }

    @Transactional
    public CheckInResponse updateCheckIn(Long checkInId, CheckInUpdateRequest request) {
        CheckIn checkIn = findCheckInById(checkInId);
        checkIn.update(
                request.getStatus(),
                request.getProgressRate(),
                request.getMemo()
        );
        return CheckInResponse.from(checkIn);
    }

    @Transactional
    public void deleteCheckIn(Long checkInId) {
        CheckIn checkIn = findCheckInById(checkInId);
        checkInRepository.delete(checkIn);
    }

    public CheckIn findCheckInById(Long checkInId) {
        return checkInRepository.findById(checkInId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHECK_IN_NOT_FOUND));
    }
}
