package com.hoeseok.yearly_goal_tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 신규 가입 허용 정책. app.signup.allowed-emails(쉼표 구분)가 비어 있으면 누구나 가입할 수 있고(로컬 개발 기본값),
 * 값이 있으면 목록에 있는 이메일만 가입할 수 있다. 이미 가입된 계정의 로그인에는 영향이 없다.
 */
@Component
public class SignupPolicy {

    private final Set<String> allowedEmails;

    public SignupPolicy(@Value("${app.signup.allowed-emails:}") String allowedEmails) {
        this.allowedEmails = Arrays.stream(allowedEmails.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .map(email -> email.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isAllowed(String email) {
        if (allowedEmails.isEmpty()) {
            return true;
        }
        return email != null && allowedEmails.contains(email.trim().toLowerCase(Locale.ROOT));
    }
}
