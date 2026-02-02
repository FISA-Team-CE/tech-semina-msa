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


    /**
     * Creates a new PointMaster for the given user with the specified initial point balance and sets the last-updated timestamp to now.
     *
     * @param userUuid the unique identifier of the user
     * @param currentAmt the initial point balance
     */
    public PointMaster(String userUuid, long currentAmt) {
        this.userUuid = userUuid;
        this.currentAmt = currentAmt;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * Increases the current point balance by the specified amount.
     *
     * Also updates {@code lastUpdatedAt} to the current date and time.
     *
     * @param amount the amount of points to add to the current balance
     */
    public void charge(long amount) {
        this.currentAmt += amount;
        this.lastUpdatedAt = LocalDateTime.now();
    }


    /**
     * Deducts the specified number of points from this entity's balance and updates the last-updated timestamp.
     *
     * @param amount the number of points to deduct
     * @throws IllegalStateException if the current balance is less than {@code amount}
     */
    public void use(long amount) {
        if (this.currentAmt < amount) {
            throw new IllegalStateException("포인트 잔액이 부족합니다.");
        }
        this.currentAmt -= amount;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * Adds the specified amount of points back to the current balance and updates the last-updated timestamp.
     *
     * @param amount the number of points to refund to the balance
     */
    public void refund(long amount) {
        this.currentAmt += amount;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}