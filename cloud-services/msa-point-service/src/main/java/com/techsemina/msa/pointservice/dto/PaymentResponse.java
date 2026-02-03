package com.techsemina.msa.pointservice.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResponse {
    private String message;   // "결제 요청이 접수되었습니다."
    private String orderId;   // 추적용 ID (UserUUID + Time 등)
    private String status;    // "PENDING" (처리중)
}