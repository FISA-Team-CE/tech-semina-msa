package com.techsemina.msa.pointservice.repository;

import com.techsemina.msa.pointservice.domain.PointMaster;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;

public interface PointMasterRepository extends JpaRepository<PointMaster, Long> {

    /**
     * 단순 조회용 (락 없음)
     * - 잔액 확인할 때 사용 (로그인 후 메인화면 등)
     */
    Optional<PointMaster> findByUserUuid(String userUuid);

    /**
     * 결제/차감용 (비관적 락 적용)
     * - 수정하는 동안 건드릴 수 없음 (SELECT ... FOR UPDATE)
     * - 동시성 문제 해결
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")}) // 3초 대기 후 에러
    @Query("select p from PointMaster p where p.userUuid = :userUuid") // 직접 쿼리 명시
    Optional<PointMaster> findByUserUuidWithLock(String userUuid);
}