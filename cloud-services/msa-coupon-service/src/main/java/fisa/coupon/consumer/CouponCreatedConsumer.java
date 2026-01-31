package fisa.coupon.consumer;

import fisa.coupon.dto.CouponIssueEvent;
import fisa.coupon.entity.Coupon;
import fisa.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponCreatedConsumer {

    private final CouponRepository couponRepository;

    @KafkaListener(topics = "coupon_issue", groupId = "coupon-group")
    public void create(CouponIssueEvent event) {
        try {
            log.info("✉️ Kafka 메시지 수신: {}", event);

            Coupon coupon = Coupon.builder()
                    .userUuid(event.getUserUuid())
                    .couponCode(event.getCouponCode())
                    .description(event.getDescription())
                    .build();

            couponRepository.save(coupon);
            log.info("💾 [발급 완료] User {} 님에게 쿠폰 {} 발급 성공!", event.getUserUuid(), event.getCouponCode());

        } catch (DataIntegrityViolationException e) {
            // DB 유니크 제약조건 위반 시 (중복 쿠폰 코드)
            log.warn("⚠️ [중복 발급 무시] 쿠폰 코드 {} 는 이미 발급되었습니다.", event.getCouponCode());
        } catch (Exception e) {
            log.error("❌ 쿠폰 발급 처리 중 알 수 없는 에러 발생", e);
            // 실무에서는 여기서 DLQ(Dead Letter Queue)로 메시지를 보냅니다.
        }
    }
}