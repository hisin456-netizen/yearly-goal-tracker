package com.hoeseok.yearly_goal_tracker.security;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.service.OAuth2UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2UserService oAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");

        User user;
        try {
            user = oAuth2UserService.findOrLinkUser(email, name, "GOOGLE", providerId);
        } catch (CustomException e) {
            if (e.getErrorCode() != ErrorCode.SIGNUP_NOT_ALLOWED) {
                throw e;
            }
            // 가입이 허용되지 않은 Google 계정: 세션/토큰을 만들지 않고 로그인 페이지로 돌려보낸다.
            response.sendRedirect(URI.create(redirectUri).resolve("/login.html?error=signup_not_allowed").toString());
            return;
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        String encodedToken = java.net.URLEncoder.encode(token, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUri + "?token=" + encodedToken);
    }
}
