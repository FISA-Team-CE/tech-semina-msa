package com.fisa.channel_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisa.channel_service.dto.payment.DepositMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // 입금 요청 전송
    public void sendDepositRequest(DepositMessage message) {
        String topic = "bank_deposit";
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(topic, jsonMessage);
            log.info("💰 [Channel -> Kafka] 입금 요청 전송 완료: {}", jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("❌ 입금 메시지 JSON 변환 오류: {}", e.getMessage());
            throw new RuntimeException("입금 메시지 변환 실패", e);
        }
    }

}