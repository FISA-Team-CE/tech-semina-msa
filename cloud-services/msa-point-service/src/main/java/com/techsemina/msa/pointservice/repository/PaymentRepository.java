package com.techsemina.msa.pointservice.repository;

import com.techsemina.msa.pointservice.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // 주문 번호로 결제 내역 찾기 (SELECT * FROM payment WHERE order_id = ?)
    Optional<Payment> findByOrderId(String orderId);
}