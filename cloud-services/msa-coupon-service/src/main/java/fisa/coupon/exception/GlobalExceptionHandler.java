package fisa.coupon.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CouponException.class)
    public ResponseEntity<Map<String, Object>> handleCouponException(CouponException e) {
        log.warn("쿠폰 발급 실패: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", e.getErrorCode().name());
        response.put("message", e.getMessage());
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("예상치 못한 에러 발생", e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "시스템 오류가 발생했습니다.");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity
                .status(500)
                .body(response);
    }
}
