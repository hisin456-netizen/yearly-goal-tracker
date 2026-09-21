package com.hoeseok.yearly_goal_tracker.common.exception;

import com.hoeseok.yearly_goal_tracker.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("없는 경로(예: 봇의 /swagger-ui 스캔)는 500 이 아니라 404")
    void noResource_is404() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleNoResource(new NoResourceFoundException(HttpMethod.GET, "swagger-ui/index.html"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("지원하지 않는 메서드는 500 이 아니라 405")
    void methodNotSupported_is405() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleMethodNotSupported(new HttpRequestMethodNotSupportedException("DELETE"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    @DisplayName("읽을 수 없는 요청 본문은 500 이 아니라 400")
    void notReadable_is400() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleNotReadable(
                new HttpMessageNotReadableException("bad json", mock(HttpInputMessage.class)));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("그 밖의 예상 못한 예외는 여전히 500")
    void unexpected_stillReturns500() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneralException(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
