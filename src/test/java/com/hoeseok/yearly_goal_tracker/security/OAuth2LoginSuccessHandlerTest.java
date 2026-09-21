package com.hoeseok.yearly_goal_tracker.security;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.service.OAuth2UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private OAuth2UserService oAuth2UserService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private OAuth2LoginSuccessHandler handler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "redirectUri", "https://goals.example.com/oauth2-redirect.html");
        given(authentication.getPrincipal()).willReturn(oAuth2User);
        given(oAuth2User.getAttribute("email")).willReturn("stranger@test.com");
        given(oAuth2User.getAttribute("name")).willReturn("stranger");
        given(oAuth2User.getAttribute("sub")).willReturn("sub-1");
    }

    @Test
    @DisplayName("가입이 허용되지 않은 Google 계정은 토큰 없이 로그인 페이지로 돌려보낸다")
    void notAllowed_redirectsToLoginWithoutToken() throws Exception {
        given(oAuth2UserService.findOrLinkUser("stranger@test.com", "stranger", "GOOGLE", "sub-1"))
                .willThrow(new CustomException(ErrorCode.SIGNUP_NOT_ALLOWED));
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);

        assertThat(response.getRedirectedUrl())
                .isEqualTo("https://goals.example.com/login.html?error=signup_not_allowed");
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("허용된 계정은 JWT를 붙여 oauth2-redirect 로 보낸다")
    void allowed_redirectsWithToken() throws Exception {
        User user = User.builder().id(7L).email("stranger@test.com").username("stranger").build();
        given(oAuth2UserService.findOrLinkUser("stranger@test.com", "stranger", "GOOGLE", "sub-1")).willReturn(user);
        given(jwtTokenProvider.generateToken(7L, "stranger@test.com", "ROLE_USER")).willReturn("jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);

        assertThat(response.getRedirectedUrl())
                .isEqualTo("https://goals.example.com/oauth2-redirect.html?token=jwt.token");
    }

    @Test
    @DisplayName("SIGNUP_NOT_ALLOWED 가 아닌 예외는 삼키지 않고 그대로 던진다")
    void otherErrors_arePropagated() {
        given(oAuth2UserService.findOrLinkUser("stranger@test.com", "stranger", "GOOGLE", "sub-1"))
                .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> handler.onAuthenticationSuccess(
                new MockHttpServletRequest(), new MockHttpServletResponse(), authentication))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
}
