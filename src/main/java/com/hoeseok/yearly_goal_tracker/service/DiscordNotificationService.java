package com.hoeseok.yearly_goal_tracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class DiscordNotificationService {

    private static final int TIMEOUT_MS = 5000;

    private final RestClient restClient;

    public DiscordNotificationService() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT_MS);
        requestFactory.setReadTimeout(TIMEOUT_MS);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    /**
     * 여러 사용자의 알림이 한 배치에서 순차 처리되므로, 하나가 느리거나 멈춰도
     * 다른 사용자의 발송이 밀리지 않도록 비동기로 실행한다.
     */
    @Async
    public void sendMessage(String webhookUrl, String content) {
        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(Map.of("content", content))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Discord Webhook 알림 발송 실패: {}", e.getMessage());
        }
    }
}
