package fisa.coupon.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupons", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_coupon_code",
                columnNames = {"couponCode"} // 쿠폰 코드는 유일해야 함
        )
})
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 받았는지 (User UUID)
    @Column(nullable = false)
    private String userUuid;

    // 쿠폰 코드 (고유 식별자)
    @Column(nullable = false, unique = true)
    private String couponCode;

    // 쿠폰 설명
    @Column(nullable = false)
    private String description;

    // 사용 여부
    @Column(nullable = false)
    private boolean used = false;

    // 발급 일시
    @Column(updatable = false)
    private LocalDateTime issuedAt;

    @Builder
    public Coupon(String userUuid, String couponCode, String description) {
        this.userUuid = userUuid;
        this.couponCode = couponCode;
        this.description = description;
        this.issuedAt = LocalDateTime.now(); // 생성 시점 자동 기록
        this.used = false;
    }

    // 쿠폰 사용 처리 메서드 (비즈니스 로직 - 추후 적금 가입 시 사용)
    public void use() {
        if (this.used) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        this.used = true;
    }
}