package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SignupPolicy signupPolicy;

    @InjectMocks
    private OAuth2UserService oAuth2UserService;

    @Test
    @DisplayName("이미 가입된 이메일이면 허용 목록과 무관하게 로그인(연동)된다")
    void existingUser_linksWithoutPolicyCheck() {
        // given
        User existing = User.builder().id(1L).email("me@test.com").username("me").build();
        given(userRepository.findByEmail("me@test.com")).willReturn(Optional.of(existing));

        // when
        User result = oAuth2UserService.findOrLinkUser("me@test.com", "me", "GOOGLE", "sub-1");

        // then
        assertThat(result).isSameAs(existing);
        verifyNoInteractions(signupPolicy);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("새 이메일이 허용 목록에 있으면 계정을 생성한다")
    void newUser_allowed_createsAccount() {
        // given
        given(userRepository.findByEmail("partner@test.com")).willReturn(Optional.empty());
        given(signupPolicy.isAllowed("partner@test.com")).willReturn(true);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = oAuth2UserService.findOrLinkUser("partner@test.com", "partner", "GOOGLE", "sub-2");

        // then
        assertThat(result.getEmail()).isEqualTo("partner@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("새 이메일이 허용 목록에 없으면 SIGNUP_NOT_ALLOWED 이고 계정을 만들지 않는다")
    void newUser_notAllowed_rejected() {
        // given
        given(userRepository.findByEmail("stranger@test.com")).willReturn(Optional.empty());
        given(signupPolicy.isAllowed("stranger@test.com")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> oAuth2UserService.findOrLinkUser("stranger@test.com", "stranger", "GOOGLE", "sub-3"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SIGNUP_NOT_ALLOWED);
        verify(userRepository, never()).save(any(User.class));
    }
}
