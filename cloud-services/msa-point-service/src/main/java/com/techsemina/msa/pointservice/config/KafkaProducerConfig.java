package com.techsemina.msa.pointservice.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}") // properties 값 가져오기
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // 카프카 브로커 주소 (application.yml에 있어도 여기서 명시하면 더 확실함)
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Key는 String, Value는 JSON(Object)으로 직렬화하겠다 설정
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        // 메시지 전송 신뢰성 설정
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // 모든 replica 확인
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);  // 재시도 횟수
         configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // 중복 전송 방지

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}