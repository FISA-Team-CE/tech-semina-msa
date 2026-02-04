package com.techsemina.msa.pointservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsemina.msa.pointservice.domain.Payment;
import com.techsemina.msa.pointservice.dto.CashResponseDTO;
import com.techsemina.msa.pointservice.dto.CoreResultEvent;
import com.techsemina.msa.pointservice.repository.PaymentRepository;
import com.techsemina.msa.pointservice.service.PaymentService;
import com.techsemina.msa.pointservice.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaConsumer {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;  // 장부 조회용
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "core-result", groupId = "point-service-group")
    @Transactional  // 에러 발생 시 롤백 & 카프카 재시도
    public void consumeWithdrawResult(String message) throws Exception {

        log.info("📨 [Kafka] 결과 수신: {}", message);

        // DTO 변환
        CashResponseDTO result;

        try {
            // 1. 여기서 에러가 나면 catch로 점프!
            result = objectMapper.readValue(message, CashResponseDTO.class);
        } catch (Exception e) {
            // 🗑️ 2. "이 메시지는 못 쓰는 겁니다"라고 로그 남기고
            log.error("❌ 치명적 에러: JSON 형식이 잘못되어 파싱할 수 없습니다. (재시도 중단) message={}", message, e);

            // 🛑 3. 여기서 return을 안 하면 밑에서 NullPointerException 터져서 또 롤백됩니다.
            // 그냥 조용히 함수를 끝내야 Kafka가 "성공했구나" 하고 다음 메시지를 줍니다.
            return;
        }

        // 2. 성공 여부 체크
        if ("SUCCESS".equals(result.getStatus())) {
            // ✅ 성공 시: 서비스의 완료 로직 호출
            paymentService.completePayment(result.getOrderId());
        } else {
            // ❌ 실패 시: 롤백(환불) 로직 진행
            log.warn("🚨 결제 실패 수신 (사유: {}). 환불을 진행합니다.", result.getMessage());

            // (1) 장부(DB)에서 주문 조회 (orderId로 찾기)
            Payment payment = paymentRepository.findByOrderId(result.getOrderId())
                    .orElseThrow(() -> new RuntimeException("주문 정보를 찾을 수 없습니다."));

            // (2) 이미 처리된 건인지 확인 (중복 방지)
            if (!"PENDING".equals(payment.getStatus())) {
                log.info("⏭️ 이미 처리가 완료된 건입니다. (현재 상태: {}). 로직을 건너뜁니다.", payment.getStatus());
                return;
            }
            // (3) 실제 사용했던 포인트 조회
            Long usedPoint = payment.getPointAmount();

            // (4) 포인트 환불
            paymentService.compensatePayment(payment.getOrderId());

            // (5) 장부 상태 업데이트 (FAILED)
            payment.setStatus("FAILED");
//            paymentRepository.save(payment); // @Transactional 있으면 자동 저장됨 (Dirty Checking)

            log.info("✅ 포인트 {}점 환불 완료.", usedPoint);
        }

    }
}