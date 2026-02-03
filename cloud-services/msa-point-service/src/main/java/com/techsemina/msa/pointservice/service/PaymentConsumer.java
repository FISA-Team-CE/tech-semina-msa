package com.techsemina.msa.pointservice.service;

import com.techsemina.msa.pointservice.dto.CashResponseDTO;
import com.techsemina.msa.pointservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "core-withdraw-result", groupId = "point-service-group")
    public void consumeWithdrawResult(String message) {
        try {
            log.info("📨 [Kafka] 결과 수신: {}", message);
            CashResponseDTO result = objectMapper.readValue(message, CashResponseDTO.class);

            if ("SUCCESS".equals(result.getStatus())) {
                // 성공 처리
                paymentService.completePayment(result.getOrderId());
            } else {
                // 실패 -> 롤백
                paymentService.compensatePayment(result.getOrderId());
            }

        } catch (Exception e) {
            log.error("❌ 메시지 처리 중 에러", e);
        }
    }
}