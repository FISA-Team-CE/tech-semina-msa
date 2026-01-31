package fisa.coupon.service;

import fisa.coupon.dto.CouponIssueEvent;
import fisa.coupon.exception.CouponErrorCode;
import fisa.coupon.exception.CouponException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Redis Key 정의
    private static final String COUPON_COUNT_KEY = "coupon:count"; // 잔여 수량
    private static final String COUPON_USER_SET_KEY = "coupon:users"; // 발급받은 유저 목록

    public void issueCoupon(String userUuid) {
        // 1. [중복 발급 검증] Redis Set에 유저 추가 (SADD)
        Long isNewUser = redisTemplate.opsForSet().add(COUPON_USER_SET_KEY, userUuid);

        if (isNewUser == 0) {
            log.info("이미 발급받은 유저입니다. UserUUID: {}", userUuid);
            throw new CouponException(CouponErrorCode.ALREADY_ISSUED);
        }

        // 2. [재고 차감] Redis Decrement (DECR)
        Long count = redisTemplate.opsForValue().decrement(COUPON_COUNT_KEY);

        if (count < 0) {
            log.info("쿠폰이 모두 소진되었습니다.");
            // 롤백: 유저 Set에서 제거
            redisTemplate.opsForSet().remove(COUPON_USER_SET_KEY, userUuid);
            throw new CouponException(CouponErrorCode.SOLD_OUT);
        }

        // 3. [Kafka 전송] 
        String couponCode = "COUPON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        CouponIssueEvent event = new CouponIssueEvent(
            userUuid,
            couponCode,
            "신규 가입 이벤트 쿠폰"
        );

        try {
            kafkaTemplate.send("coupon_issue", event);
            log.info("✅ Kafka 전송 완료 - UserUUID: {}, CouponCode: {}", userUuid, couponCode);
        } catch (Exception e) {
            // Kafka 전송 실패 시 롤백
            redisTemplate.opsForSet().remove(COUPON_USER_SET_KEY, userUuid);
            redisTemplate.opsForValue().increment(COUPON_COUNT_KEY);
            log.error("❌ Kafka 전송 실패 - 롤백 처리", e);
            throw new CouponException(CouponErrorCode.SYSTEM_ERROR);
        }
    }
}