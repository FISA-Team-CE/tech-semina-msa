package fisa.coupon.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        System.out.println("🚩 Redis Connection -> " + host + ":" + port);

        // ===== [트래픽 대응] Connection Pool 활성화 =====
        System.out.println("✅ [운영 모드] Connection Pool 활성화 - 최대 50개 연결 사용");

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(50);      // Pod당 최대 50개 Redis 연결 (기본 8 → 50)
        poolConfig.setMaxIdle(30);       // 유휴 연결 최대 30개 유지 (재사용 대기)
        poolConfig.setMinIdle(10);       // 최소 10개는 항상 준비 (즉시 응답)
        poolConfig.setMaxWait(Duration.ofMillis(3000));  // 연결 대기 최대 3초
        poolConfig.setTestOnBorrow(true);   // 연결 가져올 때 유효성 검증
        poolConfig.setTestOnReturn(true);   // 연결 반환 시 유효성 검증
        poolConfig.setTestWhileIdle(true);  // 유휴 연결도 주기적으로 검증

        // Socket 타임아웃 설정 (네트워크 장애 대응)
        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofSeconds(3))   // 연결 타임아웃 3초
                .keepAlive(true)                         // TCP KeepAlive 활성화
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .build();

        // Lettuce Pool 설정 적용
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofSeconds(3))   // Redis 명령 타임아웃 3초
                .build();

        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(host, port);

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}