package com.hoeseok.yearly_goal_tracker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.dto.ai.SubTaskSuggestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${google.ai.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent";

    public List<SubTaskSuggestionResponse> suggestSubTasks(String goalTitle, String category, String description) {
        String prompt = buildPrompt(goalTitle, category, description);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 2048
                )
        );

        try {
            RestClient restClient = RestClient.create();
            String rawResponse = restClient.post()
                    .uri(GEMINI_URL + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            (req, res) -> {
                                String body = new String(res.getBody().readAllBytes());
                                log.error("Gemini API 오류 {} - body: {}", res.getStatusCode(), body);
                                throw new CustomException(ErrorCode.AI_SERVICE_ERROR);
                            })
                    .body(String.class);

            return parseGeminiResponse(rawResponse);

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.AI_SERVICE_ERROR);
        }
    }

    private String buildPrompt(String title, String category, String description) {
        return String.format("""
당신은 개인 목표 달성 전문 코치입니다. 다음 연간 목표에 대한 실천 가능한 서브태스크를 추천해주세요.

목표: %s
카테고리: %s
설명: %s

반드시 아래 JSON 배열 형식으로만 응답하세요. 설명 없이 JSON만 출력하세요:
[
  {"title":"서브태스크명","periodType":"DAILY","targetCount":1,"reason":"추천 이유"},
  {"title":"서브태스크명","periodType":"WEEKLY","targetCount":3,"reason":"추천 이유"}
]

periodType은 DAILY, WEEKLY, MONTHLY 중 하나. 최대 5개 추천.
""", title, category != null ? category : "기타", description != null ? description : "없음");
    }

    private List<SubTaskSuggestionResponse> parseGeminiResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String text = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText();

            log.info("Gemini 원본 응답:\n{}", text);

            // [ ... ] 범위만 추출
            int start = text.indexOf('[');
            int end = text.lastIndexOf(']') + 1;
            if (start < 0 || end <= start) {
                log.error("JSON 배열을 찾을 수 없음. 원본: {}", text);
                throw new CustomException(ErrorCode.AI_SERVICE_ERROR);
            }
            String jsonText = text.substring(start, end);

            return objectMapper.readValue(jsonText, new TypeReference<List<SubTaskSuggestionResponse>>() {});

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.AI_SERVICE_ERROR);
        }
    }
}
