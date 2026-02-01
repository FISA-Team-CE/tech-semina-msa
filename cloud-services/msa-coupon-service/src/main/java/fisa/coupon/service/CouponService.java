package fisa.coupon.service;

import fisa.coupon.dto.CouponIssueEvent;
import fisa.coupon.exception.CouponErrorCode;
import fisa.coupon.exception.CouponException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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

    // Lua 스크립트: 원자적으로 중복 체크 + 재고 차감
    private static final String LUA_SCRIPT = 
        "local userKey = KEYS[1] " +
        "local countKey = KEYS[2] " +
        "local userUuid = ARGV[1] " +
        "" +
        "-- 1. 중복 발급 체크 " +
        "if redis.call('SISMEMBER', userKey, userUuid) == 1 then " +
        "    return -1  -- 이미 발급받음 " +
        "end " +
        "" +
        "-- 2. 재고 확인 " +
        "local count = tonumber(redis.call('GET', countKey)) " +
        "if count == nil or count <= 0 then " +
        "    return -2  -- 재고 없음 " +
        "end " +
        "" +
        "-- 3. 원자적으로 처리 " +
        "redis.call('SADD', userKey, userUuid) " +
        "redis.call('DECR', countKey) " +
        "" +
        "return 1  -- 성공 ";

    public void issueCoupon(String userUuid) {
        // 1. [재고 확인 먼저!] 0 이하면 바로 거부
        String countStr = (String) redisTemplate.opsForValue().get(COUPON_COUNT_KEY);
        if (countStr == null || Long.parseLong(countStr) <= 0) {
            log.info("쿠폰이 모두 소진되었습니다.");
            throw new CouponException(CouponErrorCode.SOLD_OUT);
        }

        // 2. [중복 발급 검증] Redis Set에 유저 추가 (SADD)
        Long isNewUser = redisTemplate.opsForSet().add(COUPON_USER_SET_KEY, userUuid);

        if (isNewUser != null && isNewUser == 0) {
            log.info("이미 발급받은 유저입니다. UserUUID: {}", userUuid);
            throw new CouponException(CouponErrorCode.ALREADY_ISSUED);
        }

        // 3. [재고 차감] Redis Decrement (DECR)
        Long count = redisTemplate.opsForValue().decrement(COUPON_COUNT_KEY);

        // 4. [음수 체크] 차감 후 음수면 롤백
        if (count != null && count < 0) {
            log.info("쿠폰이 모두 소진되었습니다. (동시 요청으로 재고 소진)");
            // 롤백: 유저 Set에서 제거 + 재고 복구
            redisTemplate.opsForSet().remove(COUPON_USER_SET_KEY, userUuid);
            redisTemplate.opsForValue().increment(COUPON_COUNT_KEY);
            throw new CouponException(CouponErrorCode.SOLD_OUT);
        }

        // 5. [Kafka 전송] 
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