package com.techsemina.msa.pointservice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_POINT_MASTER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pointId;

    @Column(nullable = false, unique = true)
    private String userUuid; // 사용자 식별자

    @Column(nullable = false)
    private Long currentAmt; // 현재 총 포인트

    private LocalDateTime lastUpdatedAt;


    // 생성자 (새 유저용)
    public PointMaster(String userUuid, long currentAmt) {
        this.userUuid = userUuid;
        this.currentAmt = currentAmt;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // [로직 1] 포인트 충전 (합산)
    public void charge(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        this.currentAmt += amount;
        this.lastUpdatedAt = LocalDateTime.now();
    }


    // [로직 2] 포인트 차감
    public void use(long amount) {
        if (this.currentAmt < amount) {
            throw new IllegalStateException("포인트 잔액이 부족합니다.");
        }
        this.currentAmt -= amount;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // [로직 3] 포인트 롤백(환불)
    public void refund(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("환불 금액은 0보다 커야 합나다.");
        }
        this.currentAmt += amount;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}