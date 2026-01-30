package com.fisa.core_payment_service.service;

import com.fisa.core_payment_service.domain.IssuedCoupon;
import com.fisa.core_payment_service.dto.CouponIssueMessage;
import com.fisa.core_payment_service.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponManageService {

    private final CouponRepository couponRepository;

    @Transactional
    public void issueCoupon(CouponIssueMessage dto) {

        // Entity 생성
        IssuedCoupon coupon = IssuedCoupon.builder()
                .userUuid(dto.getUserUuid())
                .couponCode(dto.getCouponCode())
                .build();


        couponRepository.save(coupon);

        log.info("💾 [Core Service] 쿠폰 발급 완료! User: {}, Code: {}",
                dto.getUserUuid(), dto.getCouponCode());
    }
}