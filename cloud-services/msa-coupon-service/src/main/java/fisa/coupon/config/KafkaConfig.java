package fisa.coupon.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // 애플리케이션 실행 시 "coupon_issue" 토픽을 자동으로 생성
    // core-payment-service의 KafkaConsumer가 이 토픽을 수신
    @Bean
    public NewTopic couponIssueTopic() {
        return TopicBuilder.name("coupon_issue")
                .partitions(3)
                .replicas(1)
                .build();
    }
}