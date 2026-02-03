package fisa.coupon.service;

import fisa.coupon.dto.CouponIssueEvent;
import fisa.coupon.exception.CouponErrorCode;
import fisa.coupon.exception.CouponException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ExecutorService를 통한 커넥션 풀 관리
    private final ExecutorService kafkaExecutor = Executors.newFixedThreadPool(
            20, // 스레드 풀 크기
            r -> {
                Thread t = new Thread(r);
                t.setName("kafka-async-" + t.getId());
                return t;
            }
    );

    // Redis Key 정의
    private static final String COUPON_COUNT_KEY = "coupon:count"; // 잔여 수량
    private static final String COUPON_USER_SET_KEY = "coupon:users"; // 발급받은 유저 목록

    // Lua 스크립트: 원자적으로 중복 체크 + 재고 차감
    private static final String LUA_SCRIPT =
            "local userKey = KEYS[1] " +
                    "local countKey = KEYS[2] " +
                    "local userUuid = ARGV[1] " +
                    "" +
                    "if redis.call('SISMEMBER', userKey, userUuid) == 1 then " +
                    "    return -1 " +
                    "end " +
                    "" +
                    "local count = tonumber(redis.call('GET', countKey)) " +
                    "if count == nil or count <= 0 then " +
                    "    return -2 " +
                    "end " +
                    "" +
                    "redis.call('SADD', userKey, userUuid) " +
                    "redis.call('DECR', countKey) " +
                    "" +
                    "return 1 ";

    public void issueCoupon(String userUuid) {
        // 1. [원자적 처리] Lua 스크립트로 중복 체크 + 재고 차감을 한 번에
        List<String> keys = List.of(COUPON_USER_SET_KEY, COUPON_COUNT_KEY);

        log.debug("🔍 Lua 스크립트 실행 시작 - UserUUID: {}", userUuid);

        Long result = null;
        try {
            result = redisTemplate.execute(
                    RedisScript.of(LUA_SCRIPT, Long.class),
                    keys,
                    userUuid
            );
            log.debug("🔍 Lua 스크립트 결과: {}", result);
        } catch (Exception e) {
            log.error("❌ Lua 스크립트 실행 중 예외 발생", e);
            throw new CouponException(CouponErrorCode.SYSTEM_ERROR);
        }

        // 2. [결과 처리]
        if (result == null) {
            log.error("Lua 스크립트 실행 실패 - result is null");
            throw new CouponException(CouponErrorCode.SYSTEM_ERROR);
        }

        if (result == -1) {
            log.info("이미 발급받은 유저입니다. UserUUID: {}", userUuid);
            throw new CouponException(CouponErrorCode.ALREADY_ISSUED);
        }

        if (result == -2) {
            log.info("쿠폰이 모두 소진되었습니다.");
            throw new CouponException(CouponErrorCode.SOLD_OUT);
        }

        // 3. [Kafka 전송 - 커넥션 풀을 사용한 비동기 방식]
        String couponCode = "COUPON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        CouponIssueEvent event = new CouponIssueEvent(
                userUuid,
                couponCode,
                "신규 가입 이벤트 쿠폰"
        );

        CompletableFuture<Void> kafkaFuture = CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send("coupon_issue", event).get(5, TimeUnit.SECONDS);
                log.info("✅ Kafka 전송 완료 - UserUUID: {}, CouponCode: {}", userUuid, couponCode);
            } catch (Exception e) {
                log.error("❌ Kafka 전송 실패 - UserUUID: {}", userUuid, e);
                rollbackCouponIssue(userUuid);
                throw new CompletionException(e);
            }
        }, kafkaExecutor);

        // 예외 처리
        kafkaFuture.exceptionally(ex -> {
            log.error("❌ Kafka 비동기 전송 중 예외 발생", ex);
            throw new CouponException(CouponErrorCode.SYSTEM_ERROR);
        });
    }

    // 롤백용 원자적 Lua 스크립트
    private static final String ROLLBACK_LUA_SCRIPT =
            "local userKey = KEYS[1] " +
                    "local countKey = KEYS[2] " +
                    "local userUuid = ARGV[1] " +
                    "" +
                    "redis.call('SREM', userKey, userUuid) " +
                    "redis.call('INCR', countKey) " +
                    "" +
                    "return 1 ";

    private void rollbackCouponIssue(String userUuid) {
        List<String> keys = List.of(COUPON_USER_SET_KEY, COUPON_COUNT_KEY);

        redisTemplate.execute(
                RedisScript.of(ROLLBACK_LUA_SCRIPT, Long.class),
                keys,
                userUuid
        );

        log.info("🔄 롤백 완료 - UserUUID: {}", userUuid);
    }

    // 애플리케이션 종료 시 ExecutorService 정리
    @jakarta.annotation.PreDestroy
    public void shutdown() {
        log.info("🔄 Kafka ExecutorService 종료 시작");
        kafkaExecutor.shutdown();
        try {
            if (!kafkaExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                kafkaExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            kafkaExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("✅ Kafka ExecutorService 종료 완료");
    }
}