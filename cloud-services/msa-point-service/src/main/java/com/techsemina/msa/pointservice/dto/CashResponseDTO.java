package com.techsemina.msa.pointservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CashResponseDTO {
    private String orderId;   // 주문 번호
    private String loginId;
    private String status;    // "SUCCESS" or "FAIL"
    private String message;   // 실패 사유 (예: "잔액 부족")
}