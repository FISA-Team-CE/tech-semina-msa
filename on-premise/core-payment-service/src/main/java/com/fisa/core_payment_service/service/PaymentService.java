package com.fisa.core_payment_service.service;

import com.fisa.core_payment_service.domain.Account;
import com.fisa.core_payment_service.dto.CouponIssueMessage;
import com.fisa.core_payment_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final AccountRepository accountRepository;
    private final KafkaProducerService kafkaProducerService;

    // 계좌 개설
    @Transactional
    public void createAccount(String accountNo, String userUuid) {
        if (accountRepository.existsById(accountNo)) {
            throw new IllegalArgumentException("이미 존재하는 계좌입니다.");
        }

        // 1. 계좌 저장
        accountRepository.save(Account.create(accountNo, userUuid));
        log.info("✅ 계좌 개설 완료: accountNo={}, user={}", accountNo, userUuid);

        // 2. 쿠폰 발급 요청 보내기 (Producer)
        try {
            CouponIssueMessage couponEvent = new CouponIssueMessage(
                    userUuid,
                    "WELCOME_COUPON",
                    "계좌 개설 축하 쿠폰"
            );
            // coupon-service가 듣고 있는 "coupon_issue" 토픽으로 쏜다
            kafkaProducerService.send("coupon_issue", couponEvent);

        } catch (Exception e) {
            log.error("⚠️ 계좌는 생성되었으나 쿠폰 발급 요청 실패: {}", e.getMessage());
        }
    }

    // 입금
    @Transactional
    public BigDecimal deposit(String accountNo, String userUuid, BigDecimal amount) {
        Account account = accountRepository.findById(accountNo)
                .orElseThrow(() -> new IllegalArgumentException("계좌가 없습니다."));

        account.validateOwner(userUuid);
        account.deposit(amount);

        return account.getBalance();
    }

    // 출금
    @Transactional
    public BigDecimal withdraw(String accountNo, String userUuid, BigDecimal amount) {
        Account account = accountRepository.findById(accountNo)
                .orElseThrow(() -> new IllegalArgumentException("계좌가 없습니다."));

        account.validateOwner(userUuid);
        account.withdraw(amount);

        return account.getBalance();
    }

    // [Saga용] 계좌번호 없이 ID로 출금
    @Transactional
    public void withdrawByLoginId(String loginId, Long amount) {
        Account account = accountRepository.findByUserUuid(loginId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 계좌가 존재하지 않습니다."));

        // Long -> BigDecimal 변환 후 출금 처리
        account.withdraw(BigDecimal.valueOf(amount));

        log.info("📉 [Saga] 출금 처리 완료: user={}, amount={}", loginId, amount);
    }

    // 잔액 조회
    public BigDecimal getBalance(String accountNo) {
        return accountRepository.findById(accountNo)
                .map(Account::getBalance)
                .orElseThrow(() -> new IllegalArgumentException("계좌가 없습니다."));
    }
}