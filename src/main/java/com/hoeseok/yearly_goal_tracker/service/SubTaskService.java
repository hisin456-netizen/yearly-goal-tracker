package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.Goal;
import com.hoeseok.yearly_goal_tracker.domain.SubTask;
import com.hoeseok.yearly_goal_tracker.domain.enums.TaskStatus;
import com.hoeseok.yearly_goal_tracker.dto.subtask.SubTaskCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.subtask.SubTaskResponse;
import com.hoeseok.yearly_goal_tracker.dto.subtask.SubTaskUpdateRequest;
import com.hoeseok.yearly_goal_tracker.repository.SubTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubTaskService {

    private final SubTaskRepository subTaskRepository;
    private final GoalService goalService;

    @Transactional
    public SubTaskResponse createSubTask(Long goalId, SubTaskCreateRequest request) {
        Goal goal = goalService.findGoalById(goalId);

        SubTask subTask = SubTask.builder()
                .goal(goal)
                .title(request.getTitle())
                .periodType(request.getPeriodType())
                .targetCount(request.getTargetCount())
                .status(TaskStatus.IN_PROGRESS)
                .build();

        SubTask saved = subTaskRepository.save(subTask);
        return SubTaskResponse.from(saved);
    }

    public List<SubTaskResponse> getSubTasksByGoalId(Long goalId, TaskStatus status) {
        List<SubTask> list;
        if (status != null) {
            list = subTaskRepository.findByGoalIdAndStatusOrderByCreatedAtAsc(goalId, status);
        } else {
            list = subTaskRepository.findByGoalIdOrderByCreatedAtAsc(goalId);
        }

        return list.stream()
                .map(SubTaskResponse::from)
                .collect(Collectors.toList());
    }

    public SubTaskResponse getSubTaskById(Long subTaskId) {
        SubTask subTask = findSubTaskById(subTaskId);
        return SubTaskResponse.from(subTask);
    }

    @Transactional
    public SubTaskResponse updateSubTask(Long subTaskId, SubTaskUpdateRequest request) {
        SubTask subTask = findSubTaskById(subTaskId);
        subTask.update(
                request.getTitle(),
                request.getPeriodType(),
                request.getTargetCount(),
                request.getStatus()
        );
        return SubTaskResponse.from(subTask);
    }

    @Transactional
    public void deleteSubTask(Long subTaskId) {
        SubTask subTask = findSubTaskById(subTaskId);
        subTaskRepository.delete(subTask);
    }

    public SubTask findSubTaskById(Long subTaskId) {
        return subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUB_TASK_NOT_FOUND));
    }
}
