package com.hoeseok.yearly_goal_tracker.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

import java.util.List;

/**
 * standaloneSetup 컨트롤러 테스트에서 @AuthenticationPrincipal 을 실제 JWT 필터와 같은 형태(principal = userId)로 재현한다.
 */
final class AuthTestSupport {

    private AuthTestSupport() {
    }

    static AuthenticationPrincipalArgumentResolver principalResolver() {
        return new AuthenticationPrincipalArgumentResolver();
    }

    static void loginAs(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    static void logout() {
        SecurityContextHolder.clearContext();
    }
}
