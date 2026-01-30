package com.fisa.core_payment_service.repository;

import com.fisa.core_payment_service.domain.IssuedCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<IssuedCoupon, Long> {
}
