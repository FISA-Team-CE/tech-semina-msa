package com.fisa.core_payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DepositMessage {
    private String userUuid;
    private String accountNo;
    private BigDecimal amount;
}