package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.Goal;
import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalCategory;
import com.hoeseok.yearly_goal_tracker.domain.enums.GoalStatus;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalDetailResponse;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalResponse;
import com.hoeseok.yearly_goal_tracker.dto.goal.GoalUpdateRequest;
import com.hoeseok.yearly_goal_tracker.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserService userService;

    @Transactional
    public GoalResponse createGoal(GoalCreateRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new CustomException(ErrorCode.INVALID_GOAL_PERIOD);
        }

        User user = userService.findUserById(request.getUserId());

        Goal goal = Goal.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(GoalStatus.IN_PROGRESS)
                .targetProgressRate(request.getTargetProgressRate() != null ? request.getTargetProgressRate() : 100)
                .build();

        Goal savedGoal = goalRepository.save(goal);
        return GoalResponse.from(savedGoal);
    }

    public List<GoalResponse> getGoals(Long userId, GoalCategory category, GoalStatus status) {
        List<Goal> goals;
        if (category != null) {
            goals = goalRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category);
        } else if (status != null) {
            goals = goalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        } else {
            goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        return goals.stream()
                .map(GoalResponse::from)
                .collect(Collectors.toList());
    }

    public GoalDetailResponse getGoalDetail(Long goalId) {
        Goal goal = goalRepository.findByIdWithSubTasks(goalId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_NOT_FOUND));
        return GoalDetailResponse.from(goal);
    }

    @Transactional
    public GoalResponse updateGoal(Long goalId, GoalUpdateRequest request) {
        Goal goal = findGoalById(goalId);

        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new CustomException(ErrorCode.INVALID_GOAL_PERIOD);
            }
        } else if (request.getStartDate() != null && request.getStartDate().isAfter(goal.getEndDate())) {
            throw new CustomException(ErrorCode.INVALID_GOAL_PERIOD);
        } else if (request.getEndDate() != null && goal.getStartDate().isAfter(request.getEndDate())) {
            throw new CustomException(ErrorCode.INVALID_GOAL_PERIOD);
        }

        goal.update(
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStatus(),
                request.getTargetProgressRate()
        );

        return GoalResponse.from(goal);
    }

    @Transactional
    public GoalResponse updateGoalStatus(Long goalId, GoalStatus status) {
        Goal goal = findGoalById(goalId);
        goal.updateStatus(status);
        return GoalResponse.from(goal);
    }

    @Transactional
    public void deleteGoal(Long goalId) {
        Goal goal = findGoalById(goalId);
        goalRepository.delete(goal);
    }

    public Goal findGoalById(Long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_NOT_FOUND));
    }
}
