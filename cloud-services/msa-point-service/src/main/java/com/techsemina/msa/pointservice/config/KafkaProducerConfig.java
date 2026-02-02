package com.techsemina.msa.pointservice.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
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

    /**
     * Creates a ProducerFactory<String, Object> configured for a local Kafka broker and JSON value serialization.
     *
     * <p>The factory is configured with bootstrap server "localhost:9092", a String key serializer,
     * and a Jackson JSON value serializer.</p>
     *
     * @return a ProducerFactory configured to produce String keys and JSON-serialized Object values
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // 카프카 브로커 주소 (application.yml에 있어도 여기서 명시하면 더 확실함)
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Key는 String, Value는 JSON(Object)으로 직렬화하겠다 설정
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Provides a KafkaTemplate wired with the configured ProducerFactory for sending messages to Kafka.
     *
     * @return a KafkaTemplate configured with the application's ProducerFactory
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}