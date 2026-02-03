package com.fisa.core_payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public void send(String topic, Object payload) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, jsonMessage);
            log.info("🚀 [Core-Producer] Sent to {}: {}", topic, jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("❌ [Core-Producer] Serialization failed: {}", payload, e);
            throw new RuntimeException("Kafka message serialization failed", e);
        }
    }
}