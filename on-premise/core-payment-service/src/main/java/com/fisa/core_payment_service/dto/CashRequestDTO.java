package com.fisa.core_payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CashRequestDTO {
    private String orderId; // 트랜잭션 ID (Saga 추적용)
    private String loginId; // 사용자 식별자 (userUuid)
    private Long amount;    // 출금액
}