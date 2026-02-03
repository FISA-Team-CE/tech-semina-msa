package com.fisa.core_payment_service.repository;

import com.fisa.core_payment_service.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {

    // uuid로 계좌 조회
    Optional<Account> findByUserUuid(String userUuid);
}