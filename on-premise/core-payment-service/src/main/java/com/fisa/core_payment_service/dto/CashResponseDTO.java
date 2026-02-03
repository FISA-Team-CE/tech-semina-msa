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
    private String orderId;
    private String userUuid;
    private String status;   // "SUCCESS" or "FAIL"
    private String message;
}