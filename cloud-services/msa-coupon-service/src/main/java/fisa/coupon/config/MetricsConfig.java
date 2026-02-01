package fisa.coupon.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void registerCustomMetrics() {
        // Redis 쿠폰 재고 메트릭 등록
        Gauge.builder("coupon.stock", this, MetricsConfig::getCouponStock)
             .description("남은 쿠폰 재고")
             .register(meterRegistry);

        // 발급받은 유저 수
        Gauge.builder("coupon.issued.users", this, MetricsConfig::getIssuedUserCount)
             .description("쿠폰을 발급받은 유저 수")
             .register(meterRegistry);
    }

    private double getCouponStock() {
        try {
            String count = (String) redisTemplate.opsForValue().get("coupon:count");
            return count != null ? Double.parseDouble(count) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double getIssuedUserCount() {
        try {
            Long size = redisTemplate.opsForSet().size("coupon:users");
            return size != null ? size.doubleValue() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
