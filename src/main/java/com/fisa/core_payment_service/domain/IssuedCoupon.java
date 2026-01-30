package com.fisa.core_payment_service.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_ISSUED_COUPON")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class IssuedCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userUuid;

    @Column(nullable = false, unique = true) // 쿠폰 코드는 중복 불가
    private String couponCode;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime issuedAt;

    @Builder
    public IssuedCoupon(String userUuid, String couponCode) {
        this.userUuid = userUuid;
        this.couponCode = couponCode;
    }
}