package com.techsemina.msa.pointservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    //주문/결제 고유 번호 (UUID 등을 사용)
    private String orderId;
    private String loginId;      // 사용자 ID
    private Long pointAmount;   // 포인트 사용액
    private Long cashAmount;    // 현금 결제액
}