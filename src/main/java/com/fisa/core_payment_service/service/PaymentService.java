package com.fisa.core_payment_service.service;

import com.fisa.core_payment_service.domain.Account;
import com.fisa.core_payment_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final AccountRepository accountRepository;

    // 계좌 개설
    @Transactional
    public void createAccount(String accountNo, String userUuid) {
        if (accountRepository.existsById(accountNo)) {
            throw new IllegalArgumentException("이미 존재하는 계좌입니다.");
        }
        accountRepository.save(Account.create(accountNo, userUuid));
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

    // 잔액 조회
    public BigDecimal getBalance(String accountNo) {
        return accountRepository.findById(accountNo)
                .map(Account::getBalance)
                .orElseThrow(() -> new IllegalArgumentException("계좌가 없습니다."));
    }
}