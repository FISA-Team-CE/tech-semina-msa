package com.techsemina.msa.pointservice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // 날짜 자동 기록용
@Table(name = "payment_history") // 테이블 이름
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 생성된 주문번호 (OrderIdGenerator)
    @Column(unique = true, nullable = false)
    private String orderId;

    private String userId;      // 누가
    private Long pointAmount;   // 포인트 얼마
    private Long cashAmount;    // 현금 얼마

    // 상태 관리 (PENDING -> COMPLETED / FAILED)
    private String status;

    @CreatedDate
    private LocalDateTime createdAt;
}