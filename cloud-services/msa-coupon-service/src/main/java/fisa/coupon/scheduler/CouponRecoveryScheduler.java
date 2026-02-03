package fisa.coupon.scheduler;

import fisa.coupon.dto.CouponIssueEvent;
import fisa.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponRecoveryScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final CouponRepository couponRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Redis Key (CouponService와 동일한 키 사용)
    private static final String COUPON_USER_SET_KEY = "coupon:users";

    // 1분마다 실행 (운영 정책에 따라 조절)
    @Scheduled(fixedDelay = 60000)
    public void recoverMissingCoupons() {
        // 1. Redis에 기록된 모든 당첨자 가져오기 (시연 규모가 작다면 SMEMBERS 사용 가능)
        // 주의: 당첨자가 수백만 명이라면 SCAN 명령어를 써야 하지만, 시연용(100~1000명)은 이걸로 충분합니다.
        Set<String> redisUsers = redisTemplate.opsForSet().members(COUPON_USER_SET_KEY);

        if (redisUsers == null || redisUsers.isEmpty()) {
            return;
        }

        log.info("누락된 쿠폰이 있는지 점검 시작. 대상 유저 수: {}", redisUsers.size());

        for (String userUuid : redisUsers) {
            // 2. DB에 실제로 저장되어 있는지 확인
            boolean exists = couponRepository.existsByUserUuid(userUuid);

            if (!exists) {
                // 3. 발견! Redis엔 있는데 DB엔 없는 유령 유저 (서버 다운 희생자)
                log.warn("🚨 누락된 발급 건 발견! UserUUID: {} -> Kafka 재전송", userUuid);

                // 고유한 쿠폰 코드 생성
                String couponCode = "RECOVERY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                // Kafka로 다시 이벤트 전송 (구제 처리)
                CouponIssueEvent event = new CouponIssueEvent(
                        userUuid,
                        couponCode,
                        "복구된 쿠폰 (시스템 장애 보상)"
                );
                kafkaTemplate.send("coupon_issue", event);
            }
        }
    }
}