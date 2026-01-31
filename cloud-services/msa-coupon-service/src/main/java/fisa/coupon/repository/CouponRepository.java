package fisa.coupon.repository;

import fisa.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    // 특정 사용자의 쿠폰 존재 여부 확인 (Recovery Scheduler용)
    boolean existsByUserUuid(String userUuid);

    // 나중에 "내 쿠폰함 조회" 기능
    List<Coupon> findAllByUserUuid(String userUuid);
}