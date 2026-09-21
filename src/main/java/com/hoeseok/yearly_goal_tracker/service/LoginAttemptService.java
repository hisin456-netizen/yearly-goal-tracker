package com.hoeseok.yearly_goal_tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 로그인 무차별 대입(brute force) 방지용 실패 횟수 제한.
 *
 * <p>최근 {@code window} 안에 같은 이메일로 {@code maxAttempts}회, 같은 IP로 {@code maxAttempts * 4}회 실패하면
 * 그 키에 대한 로그인 시도를 차단한다. 슬라이딩 윈도우라서 가장 오래된 실패가 윈도우를 벗어나면 자동으로 풀린다.
 * 차단된 시도는 실패로 다시 세지 않으므로 차단 시간이 계속 늘어나지 않는다.
 *
 * <p>이메일 기준 제한은 X-Forwarded-For 위조와 무관하게 동작한다. 존재하지 않는 이메일도 똑같이 세기 때문에
 * 차단 여부로 가입 여부를 알아낼 수 없다. 단일 인스턴스 메모리 기반이며 재시작하면 초기화된다.
 */
@Component
public class LoginAttemptService {

    /** 무작위 이메일을 계속 보내 메모리를 채우는 공격을 막기 위한 추적 키 상한 */
    private static final int MAX_TRACKED_KEYS = 10_000;
    private static final String UNKNOWN_IP = "unknown";

    private final int maxAttemptsPerEmail;
    private final int maxAttemptsPerIp;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Long>> failures = new HashMap<>();

    @Autowired
    public LoginAttemptService(
            @Value("${app.security.login.max-attempts:5}") int maxAttempts,
            @Value("${app.security.login.window-minutes:15}") long windowMinutes) {
        this(maxAttempts, maxAttempts * 4, Duration.ofMinutes(windowMinutes), Clock.systemUTC());
    }

    LoginAttemptService(int maxAttemptsPerEmail, int maxAttemptsPerIp, Duration window, Clock clock) {
        this.maxAttemptsPerEmail = maxAttemptsPerEmail;
        this.maxAttemptsPerIp = maxAttemptsPerIp;
        this.window = window;
        this.clock = clock;
    }

    public synchronized boolean isBlocked(String ip, String email) {
        return recentFailures(emailKey(email)) >= maxAttemptsPerEmail
                || recentFailures(ipKey(ip)) >= maxAttemptsPerIp;
    }

    public synchronized void recordFailure(String ip, String email) {
        long now = clock.millis();
        if (failures.size() >= MAX_TRACKED_KEYS) {
            purgeExpired(now);
        }
        add(emailKey(email), now);
        add(ipKey(ip), now);
    }

    /** 로그인에 성공하면 그 이메일의 실패 기록을 지운다. (IP 기록은 다른 계정 공격 추적을 위해 유지) */
    public synchronized void recordSuccess(String email) {
        failures.remove(emailKey(email));
    }

    private void add(String key, long now) {
        Deque<Long> deque = failures.get(key);
        if (deque == null) {
            if (failures.size() >= MAX_TRACKED_KEYS) {
                return; // 상한에 도달하면 새 키는 추적하지 않는다 (이미 추적 중인 키는 계속 제한됨)
            }
            deque = new ArrayDeque<>();
            failures.put(key, deque);
        }
        deque.addLast(now);
    }

    private int recentFailures(String key) {
        Deque<Long> deque = failures.get(key);
        if (deque == null) {
            return 0;
        }
        long cutoff = clock.millis() - window.toMillis();
        while (!deque.isEmpty() && deque.peekFirst() <= cutoff) {
            deque.removeFirst();
        }
        if (deque.isEmpty()) {
            failures.remove(key);
            return 0;
        }
        return deque.size();
    }

    private void purgeExpired(long now) {
        long cutoff = now - window.toMillis();
        failures.values().removeIf(deque -> deque.isEmpty() || deque.peekLast() <= cutoff);
    }

    private static String emailKey(String email) {
        return "email:" + (email == null ? "" : email.trim().toLowerCase(Locale.ROOT));
    }

    private static String ipKey(String ip) {
        return "ip:" + (ip == null || ip.isBlank() ? UNKNOWN_IP : ip.trim());
    }
}
