package com.fisa.channel_service.controller;

import com.fisa.channel_service.dto.payment.AmountRequestDto;
import com.fisa.channel_service.service.BankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/channel/banking")
@RequiredArgsConstructor
public class BankingController {

    private final BankingService bankingService;

    // 계좌 개설
    @PostMapping("/accounts")
    public String createAccount(@AuthenticationPrincipal String userUuid, // ★ 필터가 넣어준 값
                                @RequestParam String accountNo) {
        bankingService.createAccount(userUuid, accountNo);
        return "계좌 개설 성공";
    }

    // 입금 (본인 계좌만 가능하게 변경)
    @PostMapping("/accounts/{accountNo}/deposit")
    public BigDecimal deposit(@AuthenticationPrincipal String userUuid,
                              @PathVariable String accountNo,
                              @RequestBody AmountRequestDto request) {

        log.info("📢 [컨트롤러 도달] 입금 요청 옴! 계좌: {}, 금액: {}", accountNo, request.amount());
        return bankingService.deposit(userUuid, accountNo, request.amount());
    }

    // 출금 (본인 계좌만 가능하게 변경)
    @PostMapping("/accounts/{accountNo}/withdraw")
    public String withdraw(@AuthenticationPrincipal String userUuid,
                           @PathVariable String accountNo,
                           @RequestBody AmountRequestDto request) {
        // userUuid를 같이 넘김
        bankingService.withdraw(userUuid, accountNo, request.amount());
        return "출금 요청 완료";
    }
}