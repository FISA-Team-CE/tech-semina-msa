package fisa.coupon.service;

import fisa.coupon.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // 1. 기존 데이터 초기화
        couponRepository.deleteAll(); // DB 비우기

        // Redis 초기화 (모든 데이터 삭제)
        // 주의: 운영 중인 Redis라면 flushAll은 절대 금지. 테스트용 Redis에서만 사용.
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 2. [핵심] 테스트를 위한 초기 재고 설정 (100개)
        // Service 로직이 이 키(coupon:count)를 감소시키며 동작하므로 필수.
        redisTemplate.opsForValue().set("coupon:count", "100");
    }

    @Test
    @DisplayName("1000명이 동시에 응모해도 100개만 발급되어야 한다 (Redis -> Kafka -> DB)")
    void concurrencyTest() throws InterruptedException {
        // Given
        int threadCount = 1000; // 요청 인원
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 32개 스레드로 병렬 공격
        CountDownLatch latch = new CountDownLatch(threadCount); // 모든 요청이 끝날 때까지 대기용

        // When
        for (int i = 0; i < threadCount; i++) {
            // userUuid를 String으로 생성 (UUID 형식)
            String userUuid = UUID.randomUUID().toString();
            executorService.submit(() -> {
                try {
                    // 서비스 메서드 호출 (userUuid는 String 타입)
                    couponService.issueCoupon(userUuid);
                } catch (Exception e) {
                    // 재고 소진(RuntimeException)이나 중복 발급 에러가 발생,
                    // 테스트 흐름을 끊지 않기 위해 로그만 찍고 넘어감.
                    // System.out.println(e.getMessage());
                } finally {
                    latch.countDown(); // 성공이든 실패든 카운트 감소
                }
            });
        }

        latch.await(); // 1000명 요청이 다 끝날 때까지 대기

        // 비동기 시스템이므로 요청이 끝나자마자 DB를 조회하면 아직 0건일 수 있다.
        Thread.sleep(10000); // 넉넉하게 10초 대기

        // Then
        long count = couponRepository.count();

        System.out.println("=== 최종 발급된 쿠폰 개수: " + count + " ===");

        // 1000명 요청 -> 100개 재고 -> 정확히 100개만 생성되어야 함
        assertThat(count).isEqualTo(100);
    }
}