package com.techsemina.msa.pointservice.exception;

import com.techsemina.msa.pointservice.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice // 모든 컨트롤러의 에러를 여기서 잡음
public class GlobalExceptionHandler {

    // 1. 비즈니스 로직 에러 (예: 잘못된 입력값, 포인트 부족 등)
    // 서비스에서 throw new IllegalArgumentException("포인트 부족") 했을 때 여기로 옴
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        log.warn("🚨 잘못된 요청 발생: {}", e.getMessage());

        ErrorResponse response = new ErrorResponse("BAD_REQUEST", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 2. 시스템 에러 (예: DB 다운, Kafka 연결 실패, NullPointer 등)
    // 위에서 안 잡힌 "나머지 모든 에러"는 여기서 잡힘
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleServerException(Exception e) {
        log.error("🔥 서버 내부 치명적 오류 발생", e); // 스택 트레이스 로그 남기기

        ErrorResponse response = new ErrorResponse("INTERNAL_SERVER_ERROR", "시스템 오류가 발생했습니다. 관리자에게 문의하세요.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}