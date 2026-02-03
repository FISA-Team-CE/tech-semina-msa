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
        
        // ===== [비교 테스트] Connection Pool 비활성화 (기본 설정 사용) =====
        // 기본 설정: MaxTotal=8, MaxIdle=8 (매우 제한적)
        System.out.println("⚠️ [테스트 모드] Connection Pool 비활성화 - 기본 8개 연결만 사용");
        
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(serverConfig);
        
        /* ===== [트래픽 대응] Connection Pool 설정 (주석 처리) =====
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
        */
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // 모든 Serializer를 StringRedisSerializer로 통일
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        
        // Transaction Support 비활성화 (성능 향상)
        redisTemplate.setEnableTransactionSupport(false);

        return redisTemplate;
    }
}