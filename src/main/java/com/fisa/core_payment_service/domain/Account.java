package com.fisa.core_payment_service.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_ACCOUNT")
public class Account {

    @Id
    @Column(name = "ACCOUNT_NO", length = 20)
    private String accountNo; // 계좌번호

    @Column(name = "USER_UUID", nullable = false)
    private String userUuid;

    @Column(name = "BALANCE", nullable = false)
    private BigDecimal balance;

    // 낙관적 락 (동시 출금 방지)
    @Version
    private Long version;

    // 계좌 개설
    public static Account create(String accountNo, String userUuid) {
        Account account = new Account();
        account.accountNo = accountNo;
        account.userUuid = userUuid;
        account.balance = BigDecimal.ZERO; // 초기 잔액 0원
        return account;
    }

    // 입금 로직
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("입금액은 0원보다 커야 합니다.");
        }
        this.balance = this.balance.add(amount);
    }

    // 출금 로직
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("출금액은 0원보다 커야 합니다.");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void validateOwner(String requestUserUuid) {
        if (!this.userUuid.equals(requestUserUuid)) {
            throw new IllegalStateException("본인의 계좌가 아닙니다.");
        }
    }
}
