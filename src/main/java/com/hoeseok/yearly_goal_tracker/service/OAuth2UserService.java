package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2UserService {

    private final UserRepository userRepository;
    private final SignupPolicy signupPolicy;

    /**
     * 소셜 로그인 이메일이 기존 가입 이메일과 같으면 그 계정에 자동 연동(기존 데이터 보존),
     * 없으면 새 계정을 생성한다. 새 계정 생성은 가입 허용 이메일만 가능하다.
     */
    @Transactional
    public User findOrLinkUser(String email, String username, String provider, String providerId) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            user.linkProvider(provider, providerId);
            return user;
        }

        if (!signupPolicy.isAllowed(email)) {
            throw new CustomException(ErrorCode.SIGNUP_NOT_ALLOWED);
        }

        User newUser = User.builder()
                .email(email)
                .username(username != null ? username : email)
                .provider(provider)
                .providerId(providerId)
                .build();
        return userRepository.save(newUser);
    }
}
