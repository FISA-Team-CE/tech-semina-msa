package com.fisa.channel_service.dto.payment;

import java.math.BigDecimal;

public record DepositMessage(
        String userUuid,
        String accountNo,
        BigDecimal amount
) {
}
