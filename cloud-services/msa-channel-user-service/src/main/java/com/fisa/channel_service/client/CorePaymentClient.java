package com.fisa.channel_service.client;

import com.fisa.channel_service.dto.payment.AmountRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

// Core-Payment 호출 (On-Premise)
@FeignClient(name = "core-payment-service", url = "${core.payment.url}")
public interface CorePaymentClient {

    // 계좌 개설
    @PostMapping("/api/core/payment/accounts")
    String createAccount(@RequestParam("accountNo") String accountNo,
                         @RequestParam("userUuid") String userUuid);

    // 입금 요청
    @PostMapping("/api/core/payment/accounts/{accountNo}/deposit")
    BigDecimal deposit(@PathVariable("accountNo") String accountNo,
                       @RequestBody AmountRequestDto request);

    // 출금 요청
    @PostMapping("/api/core/payment/accounts/{accountNo}/withdraw")
    BigDecimal withdraw(@PathVariable("accountNo") String accountNo,
                        @RequestBody AmountRequestDto request);
}