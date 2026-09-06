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
        return createCheckIn(null, subTaskId, request);
    }

    @Transactional
    public CheckInResponse createCheckIn(Long userId, Long subTaskId, CheckInCreateRequest request) {
        SubTask subTask = subTaskService.findSubTaskById(subTaskId);
        subTaskService.validateSubTaskOwner(subTask, userId);

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
        return updateCheckIn(null, checkInId, request);
    }

    @Transactional
    public CheckInResponse updateCheckIn(Long userId, Long checkInId, CheckInUpdateRequest request) {
        CheckIn checkIn = findCheckInById(checkInId);
        validateCheckInOwner(checkIn, userId);

        checkIn.update(
                request.getStatus(),
                request.getProgressRate(),
                request.getMemo()
        );
        return CheckInResponse.from(checkIn);
    }

    @Transactional
    public void deleteCheckIn(Long checkInId) {
        deleteCheckIn(null, checkInId);
    }

    @Transactional
    public void deleteCheckIn(Long userId, Long checkInId) {
        CheckIn checkIn = findCheckInById(checkInId);
        validateCheckInOwner(checkIn, userId);
        checkInRepository.delete(checkIn);
    }

    public void validateCheckInOwner(CheckIn checkIn, Long userId) {
        if (userId != null && checkIn.getSubTask() != null) {
            subTaskService.validateSubTaskOwner(checkIn.getSubTask(), userId);
        }
    }

    public CheckIn findCheckInById(Long checkInId) {
        return checkInRepository.findById(checkInId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHECK_IN_NOT_FOUND));
    }
}
