package com.fisa.core_payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CashResponseDTO {
    private String loginId;  // 사용자 식별자
    private String status;   // "SUCCESS" or "FAIL"
    private String message;
}