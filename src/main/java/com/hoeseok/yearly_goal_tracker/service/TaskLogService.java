package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.SubTask;
import com.hoeseok.yearly_goal_tracker.domain.TaskLog;
import com.hoeseok.yearly_goal_tracker.dto.tasklog.TaskLogCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.tasklog.TaskLogResponse;
import com.hoeseok.yearly_goal_tracker.repository.SubTaskRepository;
import com.hoeseok.yearly_goal_tracker.repository.TaskLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskLogService {

    private final TaskLogRepository taskLogRepository;
    private final SubTaskRepository subTaskRepository;

    public List<TaskLogResponse> getLogs(Long subTaskId, Long userId) {
        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUB_TASK_NOT_FOUND));
        if (!subTask.getGoal().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return taskLogRepository.findBySubTaskIdOrderByLogDateDesc(subTaskId)
                .stream().map(TaskLogResponse::from).toList();
    }

    @Transactional
    public TaskLogResponse create(Long subTaskId, TaskLogCreateRequest req, Long userId) {
        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUB_TASK_NOT_FOUND));
        if (!subTask.getGoal().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        TaskLog taskLog = TaskLog.builder()
                .subTask(subTask)
                .content(req.getContent())
                .imageUrl(req.getImageUrl())
                .isCorrect(req.getIsCorrect())
                .logDate(req.getLogDate() != null ? req.getLogDate() : LocalDate.now())
                .build();

        return TaskLogResponse.from(taskLogRepository.save(taskLog));
    }

    @Transactional
    public void delete(Long logId, Long userId) {
        TaskLog taskLog = taskLogRepository.findById(logId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_LOG_NOT_FOUND));
        if (!taskLog.getSubTask().getGoal().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        taskLogRepository.delete(taskLog);
    }
}
