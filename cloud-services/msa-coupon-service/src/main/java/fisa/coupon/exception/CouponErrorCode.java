package fisa.coupon.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponErrorCode {
    ALREADY_ISSUED(HttpStatus.BAD_REQUEST, "이미 발급된 쿠폰입니다."),
    SOLD_OUT(HttpStatus.BAD_REQUEST, "선착순 마감되었습니다."),
    SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "시스템 오류로 쿠폰 발급에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
