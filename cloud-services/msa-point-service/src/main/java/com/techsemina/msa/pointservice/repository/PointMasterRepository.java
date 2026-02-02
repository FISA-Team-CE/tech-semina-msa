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
 * Retrieve the PointMaster for the given user UUID without acquiring any database lock.
 *
 * @param userUuid the user's UUID used to look up the PointMaster
 * @return an Optional containing the PointMaster if found, empty otherwise
 */
    Optional<PointMaster> findByUserUuid(String userUuid);

    /**
     * Fetches the PointMaster for the given userUuid while acquiring a pessimistic write lock to prevent concurrent modifications.
     *
     * The query waits up to 3 seconds to obtain the lock; if the lock cannot be acquired within that timeout an error is raised.
     *
     * @param userUuid the UUID of the user whose PointMaster is requested
     * @return an Optional containing the PointMaster for the user if found, otherwise an empty Optional
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")}) // 3초 대기 후 에러
    @Query("select p from PointMaster p where p.userUuid = :userUuid") // 👈 직접 쿼리 명시
    Optional<PointMaster> findByUserUuidWithLock(String userUuid);
    // (JPA가 메서드 이름을 분석할 때 'AndLock'은 무시하므로 기능은 똑같이 동작하고 락만 걸립니다)
    // 혹은 @Query("select p from PointMaster p where p.userUuid = :uuid") 로 직접 짜도 됨
}