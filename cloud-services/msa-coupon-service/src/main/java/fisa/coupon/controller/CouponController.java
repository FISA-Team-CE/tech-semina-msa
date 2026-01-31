package fisa.coupon.controller;

import fisa.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    // POST http://localhost:8082/api/coupons/issue?userUuid=user-123-abc
    @PostMapping("/issue")
    public String issue(@RequestParam("userUuid") String userUuid) {

        // 1. 서비스 로직 호출 (Redis 카운팅 -> Kafka 전송)
        couponService.issueCoupon(userUuid);

        // 2. 결과와 상관없이 "접수됨" 응답 즉시 반환
        // 사용자는 기다리지 않고 바로 응답을 받습니다.
        return "쿠폰 발급 신청이 정상적으로 접수되었습니다.";
    }
}