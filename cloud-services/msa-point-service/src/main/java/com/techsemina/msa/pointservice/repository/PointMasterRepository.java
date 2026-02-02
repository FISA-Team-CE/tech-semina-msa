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
     * 🔥 [핵심 수정] 결제/차감용 (비관적 락 적용)
     * - "내가 수정하는 동안 아무도 건드리지 마!" (SELECT ... FOR UPDATE)
     * - 동시성 문제 해결의 핵심입니다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")}) // 3초 대기 후 에러
    @Query("select p from PointMaster p where p.userUuid = :userUuid") // 👈 직접 쿼리 명시
    Optional<PointMaster> findByUserUuidWithLock(String userUuid);
    // (JPA가 메서드 이름을 분석할 때 'AndLock'은 무시하므로 기능은 똑같이 동작하고 락만 걸립니다)
    // 혹은 @Query("select p from PointMaster p where p.userUuid = :uuid") 로 직접 짜도 됨
}