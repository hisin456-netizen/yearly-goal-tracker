package com.hoeseok.yearly_goal_tracker.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignupPolicyTest {

    @Test
    @DisplayName("허용 목록이 비어 있으면 누구나 가입 가능 (로컬 개발 기본값)")
    void emptyList_allowsEveryone() {
        assertThat(new SignupPolicy("").isAllowed("anyone@test.com")).isTrue();
        assertThat(new SignupPolicy("  ,  , ").isAllowed("anyone@test.com")).isTrue();
    }

    @Test
    @DisplayName("허용 목록이 있으면 목록에 있는 이메일만 가입 가능")
    void withList_allowsOnlyListed() {
        SignupPolicy policy = new SignupPolicy("me@test.com,partner@test.com");

        assertThat(policy.isAllowed("me@test.com")).isTrue();
        assertThat(policy.isAllowed("partner@test.com")).isTrue();
        assertThat(policy.isAllowed("stranger@test.com")).isFalse();
    }

    @Test
    @DisplayName("대소문자와 앞뒤 공백은 무시한다")
    void ignoresCaseAndWhitespace() {
        SignupPolicy policy = new SignupPolicy(" Me@Test.com , PARTNER@test.com ");

        assertThat(policy.isAllowed("me@test.com")).isTrue();
        assertThat(policy.isAllowed("  ME@TEST.COM ")).isTrue();
        assertThat(policy.isAllowed("partner@TEST.com")).isTrue();
    }

    @Test
    @DisplayName("허용 목록이 있는데 이메일이 null이면 거부")
    void withList_nullEmailRejected() {
        assertThat(new SignupPolicy("me@test.com").isAllowed(null)).isFalse();
    }

    @Test
    @DisplayName("부분 일치로 통과하지 않는다 (me@test.com 허용이 xme@test.com 을 허용하지 않음)")
    void noPartialMatch() {
        SignupPolicy policy = new SignupPolicy("me@test.com");

        assertThat(policy.isAllowed("xme@test.com")).isFalse();
        assertThat(policy.isAllowed("me@test.com.evil.com")).isFalse();
    }
}
