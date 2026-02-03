package com.fisa.core_payment_service.controller;

import com.fisa.core_payment_service.dto.AmountRequestDto;
import com.fisa.core_payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/core/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 계좌 생성
    @PostMapping("/accounts")
    public String createAccount(@RequestParam String accountNo, @RequestParam String userUuid) {
        paymentService.createAccount(accountNo, userUuid);
        return "계좌 개설 완료";
    }

    // 잔액 조회
    @GetMapping("/accounts/{accountNo}")
    public BigDecimal getBalance(@PathVariable String accountNo) {
        return paymentService.getBalance(accountNo);
    }

    // 입금
    @PostMapping("/accounts/{accountNo}/deposit")
    public BigDecimal deposit(@PathVariable String accountNo, @Valid @RequestBody AmountRequestDto request) {
        return paymentService.deposit(accountNo, request.userUuid(), request.amount());
    }

    // 출금
    @PostMapping("/accounts/{accountNo}/withdraw")
    public BigDecimal withdraw(@PathVariable String accountNo, @RequestBody AmountRequestDto request) {
        return paymentService.withdraw(accountNo, request.userUuid(), request.amount());
    }


}
