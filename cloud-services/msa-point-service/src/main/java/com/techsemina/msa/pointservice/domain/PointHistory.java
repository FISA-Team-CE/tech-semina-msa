package com.techsemina.msa.pointservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_POINT_HISTORY")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long histId;

    @Column(nullable = false)
    private Long pointId; // FK

    @Column(nullable = false)
    private String txType; // CHARGE(적립), USE(사용), ROLLBACK(롤백)

    private Long amount;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PointHistory(Long pointId, String txType, Long amount) {
        this.pointId = pointId;
        this.txType = txType;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }
}