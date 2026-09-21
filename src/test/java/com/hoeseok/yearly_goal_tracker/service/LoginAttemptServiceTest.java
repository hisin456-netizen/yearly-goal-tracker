package com.hoeseok.yearly_goal_tracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    /** 시간을 마음대로 움직일 수 있는 테스트용 시계 */
    private static class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-09-21T00:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private MutableClock clock;
    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        // 이메일당 5회, IP당 20회, 윈도우 15분 (운영 기본값과 동일)
        service = new LoginAttemptService(5, 20, Duration.ofMinutes(15), clock);
    }

    private void fail(String ip, String email, int times) {
        for (int i = 0; i < times; i++) {
            service.recordFailure(ip, email);
        }
    }

    @Test
    @DisplayName("실패가 한도 미만이면 차단되지 않는다")
    void belowLimit_notBlocked() {
        fail("1.1.1.1", "me@test.com", 4);

        assertThat(service.isBlocked("1.1.1.1", "me@test.com")).isFalse();
    }

    @Test
    @DisplayName("같은 이메일로 5회 실패하면 다른 IP에서도 차단된다")
    void emailLimit_blocksAcrossIps() {
        for (int i = 0; i < 5; i++) {
            service.recordFailure("9.9.9." + i, "me@test.com");
        }

        assertThat(service.isBlocked("8.8.8.8", "me@test.com")).isTrue();
    }

    @Test
    @DisplayName("이메일은 대소문자와 공백을 무시하고 센다 (대문자로 바꿔서 우회 불가)")
    void emailNormalized() {
        service.recordFailure("1.1.1.1", "Me@Test.com");
        service.recordFailure("2.2.2.2", " me@test.COM ");
        service.recordFailure("3.3.3.3", "ME@TEST.COM");
        service.recordFailure("4.4.4.4", "me@test.com");
        service.recordFailure("5.5.5.5", "mE@tEsT.cOm");

        assertThat(service.isBlocked("6.6.6.6", "me@test.com")).isTrue();
    }

    @Test
    @DisplayName("다른 이메일은 영향을 받지 않는다 (내 계정이 남 때문에 잠기지 않는다)")
    void otherEmail_notAffected() {
        fail("1.1.1.1", "victim@test.com", 5);

        assertThat(service.isBlocked("2.2.2.2", "other@test.com")).isFalse();
    }

    @Test
    @DisplayName("같은 IP에서 여러 이메일을 돌려가며 시도해도 IP 한도(20회)에서 차단된다")
    void ipLimit_blocksCredentialStuffing() {
        for (int i = 0; i < 20; i++) {
            service.recordFailure("7.7.7.7", "user" + i + "@test.com"); // 이메일마다 1회씩이라 이메일 한도는 안 걸림
        }

        assertThat(service.isBlocked("7.7.7.7", "fresh@test.com")).isTrue();
        assertThat(service.isBlocked("8.8.8.8", "fresh@test.com")).isFalse();
    }

    @Test
    @DisplayName("윈도우(15분)가 지나면 차단이 자동으로 풀린다")
    void blockLiftsAfterWindow() {
        fail("1.1.1.1", "me@test.com", 5);
        assertThat(service.isBlocked("1.1.1.1", "me@test.com")).isTrue();

        clock.advance(Duration.ofMinutes(14));
        assertThat(service.isBlocked("1.1.1.1", "me@test.com")).isTrue();

        clock.advance(Duration.ofMinutes(2));
        assertThat(service.isBlocked("1.1.1.1", "me@test.com")).isFalse();
    }

    @Test
    @DisplayName("차단 중 시도는 실패로 다시 세지 않는다 (isBlocked 만으로는 기록이 늘지 않음)")
    void checkingBlockedDoesNotExtendBlock() {
        fail("1.1.1.1", "me@test.com", 5);

        for (int i = 0; i < 100; i++) {
            service.isBlocked("1.1.1.1", "me@test.com");
        }
        clock.advance(Duration.ofMinutes(16));

        assertThat(service.isBlocked("1.1.1.1", "me@test.com")).isFalse();
    }

    @Test
    @DisplayName("오래된 실패는 윈도우 밖으로 밀려나 누적되지 않는다 (천천히 시도하면 차단되지 않음)")
    void slowAttempts_areNotAccumulated() {
        for (int i = 0; i < 20; i++) {
            service.recordFailure("1.1.1.1", "me@test.com");
            clock.advance(Duration.ofMinutes(5)); // 15분 안에는 최대 3회
        }

        assertThat(service.isBlocked("1.1.1.1", "me@test.com")).isFalse();
    }

    @Test
    @DisplayName("로그인에 성공하면 그 이메일의 실패 기록이 지워진다")
    void success_clearsEmailFailures() {
        fail("1.1.1.1", "me@test.com", 4);

        service.recordSuccess("me@test.com");
        fail("1.1.1.1", "me@test.com", 4);

        assertThat(service.isBlocked("1.1.1.1", "me@test.com")).isFalse();
    }

    @Test
    @DisplayName("IP 를 알 수 없어도(null/공백) 예외 없이 동작한다")
    void nullIp_and_nullEmail_areHandled() {
        service.recordFailure(null, null);
        service.recordFailure("  ", "  ");

        assertThat(service.isBlocked(null, null)).isFalse();
    }
}
