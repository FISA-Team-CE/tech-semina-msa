package com.techsemina.msa.pointservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String loginId;      // 사용자 ID
    private Long pointAmount;   // 포인트 사용액
    private Long cashAmount;    // 현금 결제액
}