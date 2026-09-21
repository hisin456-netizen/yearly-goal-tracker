package com.hoeseok.yearly_goal_tracker.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "이미 등록된 이메일입니다."),

    // Auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    TOO_MANY_LOGIN_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "로그인 시도가 너무 많습니다. 잠시 후 다시 시도해주세요."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    SIGNUP_NOT_ALLOWED(HttpStatus.FORBIDDEN, "가입이 허용되지 않은 이메일입니다."),

    // Goal
    GOAL_NOT_FOUND(HttpStatus.NOT_FOUND, "목표를 찾을 수 없습니다."),
    INVALID_GOAL_PERIOD(HttpStatus.BAD_REQUEST, "목표 시작일은 종료일보다 이전이어야 합니다."),

    // SubTask
    SUB_TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "하위 태스크를 찾을 수 없습니다."),

    // CheckIn
    CHECK_IN_NOT_FOUND(HttpStatus.NOT_FOUND, "체크인 기록을 찾을 수 없습니다."),
    CHECK_IN_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 날짜에 이미 체크인 기록이 존재합니다."),

    // Note
    NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "노트를 찾을 수 없습니다."),

    // TaskLog
    TASK_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "작업 기록을 찾을 수 없습니다."),

    // Schedule
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "일정을 찾을 수 없습니다."),

    // File
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "이미지 파일(png, jpg, gif, webp)만 업로드할 수 있습니다."),

    // AI
    AI_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI 서비스 호출에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
