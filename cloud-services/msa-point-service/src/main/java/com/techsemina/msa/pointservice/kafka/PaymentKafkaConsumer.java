package com.techsemina.msa.pointservice.kafka;

import com.techsemina.msa.pointservice.dto.CoreResultEvent;
import com.techsemina.msa.pointservice.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaConsumer {

    private final PointService pointService; // 직접 주입

    // 온프레미스(코어뱅킹)의 응답을 듣는 리스너
    @KafkaListener(topics = "core-result", groupId = "payment-group")
    public void handleCoreResult(CoreResultEvent event) {
        if ("SUCCESS".equals(event.getStatus())) {
            log.info("🎉 최종 결제 성공! (포인트 O, 현금 O)");
        } else {
            log.error("🚨 온프레미스 출금 실패! -> [보상 트랜잭션] 포인트 환불 진행");

            // --- Step 3: 포인트 롤백 (보상 트랜잭션) ---
            // 🔥 핵심: Kafka 안 쓰고 직접 서비스 호출해서 롤백!
            try {
                pointService.refundPoint(event.getUserId(), 5000L); // 금액은 예시
                log.info("✅ 포인트 환불(롤백) 완료. 결제가 취소되었습니다.");
            } catch (Exception e) {
                log.error("💀 큰일 났다... 환불마저 실패함. (관리자 호출 필요)");
            }
        }
    }
}