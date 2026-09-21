package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.domain.LoginHistory;
import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.dto.auth.LoginHistoryResponse;
import com.hoeseok.yearly_goal_tracker.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    /**
     * REQUIRES_NEW: 로그인 성공/실패와 무관하게, 그리고 호출부 트랜잭션의 readOnly 여부와 무관하게
     * 감사 기록은 항상 독립적으로 커밋되어야 하므로 별도 트랜잭션에서 실행한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String email, User user, boolean success, String ipAddress, String userAgent, String failReason) {
        LoginHistory history = LoginHistory.builder()
                .email(email)
                .user(user)
                .success(success)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .failReason(failReason)
                .build();
        loginHistoryRepository.save(history);
    }

    public List<LoginHistoryResponse> getRecentHistory(Long userId) {
        return loginHistoryRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(LoginHistoryResponse::from).toList();
    }
}
