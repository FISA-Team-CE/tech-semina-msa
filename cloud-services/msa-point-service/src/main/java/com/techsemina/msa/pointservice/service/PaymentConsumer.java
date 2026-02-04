package com.techsemina.msa.pointservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsemina.msa.pointservice.domain.Payment;
import com.techsemina.msa.pointservice.dto.CashResponseDTO;
import com.techsemina.msa.pointservice.dto.CoreResultEvent;
import com.techsemina.msa.pointservice.repository.PaymentRepository;
import com.techsemina.msa.pointservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;  // 장부 조회용
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "core-withdraw-result", groupId = "point-service-group")
    @Transactional  // 에러 발생 시 롤백 & 카프카 재시도
    public void consumeWithdrawResult(String message) throws Exception {

        log.info("📨 [Kafka] 결과 수신: {}", message);

        // 1. DTO 변환
        CashResponseDTO result = objectMapper.readValue(message, CashResponseDTO.class);

        // 2. 성공 여부 체크
        if ("SUCCESS".equals(result.getStatus())) {
            // ✅ 성공 시: 서비스의 완료 로직 호출
            paymentService.completePayment(result.getOrderId());
        } else {
            // ❌ 실패 시: 롤백(환불) 로직 진행
            log.warn("🚨 결제 실패 수신 (사유: {}). 환불을 진행합니다.", result.getMessage());

            // (1) 장부(DB)에서 주문 조회 (orderId로 찾기!)
            Payment payment = paymentRepository.findByOrderId(result.getOrderId())
                    .orElseThrow(() -> new RuntimeException("주문 정보를 찾을 수 없습니다."));

            // (2) 이미 처리된 건인지 확인 (중복 방지)
            if ("FAILED".equals(payment.getStatus())) {
                log.info("이미 처리된 환불 건입니다.");
                return;
            }
            // (3) 실제 사용했던 포인트 조회
            Long usedPoint = payment.getPointAmount();

            // (4) 포인트 환불
            paymentService.compensatePayment(payment.getOrderId());

            // (5) 장부 상태 업데이트 (FAILED)
            payment.setStatus("FAILED");
            paymentRepository.save(payment); // @Transactional 있으면 자동 저장됨 (Dirty Checking)

            log.info("✅ 포인트 {}점 환불 완료.", usedPoint);
        }

    }
}