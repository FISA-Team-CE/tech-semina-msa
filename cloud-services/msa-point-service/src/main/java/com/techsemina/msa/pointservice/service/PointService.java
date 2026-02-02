package com.techsemina.msa.pointservice.service;
import com.techsemina.msa.pointservice.domain.PointHistory;
import com.techsemina.msa.pointservice.domain.PointMaster;
import com.techsemina.msa.pointservice.repository.PointHistoryRepository;
import com.techsemina.msa.pointservice.repository.PointMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PointService {

    private final PointMasterRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    /**
     * Upserts a user's point balance by adding the specified amount and records a CHARGE history entry.
     *
     * If no PointMaster exists for the given userUuid, a new one is created with a zero balance before applying the charge.
     *
     * @param userUuid the UUID of the user whose points will be charged
     * @param amount the amount of points to add to the user's balance
     * @return the persisted PointMaster reflecting the updated balance
     */
    public PointMaster chargePoint(String userUuid, long amount) {
        // 1. 유저 조회 (없으면 새로 생성 - 0원으로 초기화)
        PointMaster pointMaster = pointRepository.findByUserUuid(userUuid)
                .orElse(new PointMaster(userUuid, 0));

        // 2. 금액 합산 (Entity 메서드 사용)
        pointMaster.charge(amount);

        // 3. 마스터 테이블 저장 (Insert or Update)
        PointMaster savedMaster = pointRepository.save(pointMaster);

        // 4. 히스토리 저장 (기록 남기기)
        saveHistory(savedMaster, amount, "CHARGE");

        log.info("💰 포인트 충전 완료: 사용자={}, 충전액={}, 잔액={}", userUuid, amount, savedMaster.getCurrentAmt());

        return savedMaster;
    }

    /**
     * Deducts points from a user's wallet with a pessimistic lock and records a usage history entry.
     *
     * Acquires a database lock on the user's PointMaster, verifies sufficient balance, subtracts the specified amount, and persists a corresponding PointHistory record.
     *
     * @param userId the UUID of the user whose points will be deducted
     * @param amount the amount of points to deduct
     * @throws RuntimeException if the user's wallet cannot be found or if the wallet's balance is less than {@code amount}
     */
    public void usePoint(String userId, Long amount) {
        // 1. 내 지갑 찾기 (Lock 사용)
        PointMaster wallet = pointRepository.findByUserUuidWithLock(userId)
                .orElseThrow(() -> new RuntimeException("사용자의 포인트 지갑을 찾을 수 없습니다."));

        // 2. 잔액 확인 (비즈니스 로직)
        if (wallet.getCurrentAmt() < amount) {
            throw new RuntimeException("포인트 잔액이 부족합니다!"); // -> 결제 전체 취소됨
        }

        // 3. 돈 깎기
        wallet.use(amount);

        // 4. 히스토리 저장 (사용 기록)
        saveHistory(wallet, amount, "USE");

        // 5. 기존 로그 유지
        log.info("💰 포인트 차감 완료: 사용자={}, 차감액={}, 잔액={}", userId, amount, wallet.getCurrentAmt());
    }

    /**
     * Refunds (rolls back) points to a user's wallet and records a REFUND history entry.
     *
     * Increases the user's point balance by the given amount and persists a corresponding history record.
     *
     * @param userId the user's UUID whose wallet will be refunded
     * @param amount the amount of points to refund (must be positive)
     * @throws RuntimeException if the user's wallet is not found
     */
    public void refund(String userId, Long amount) {
        PointMaster wallet = pointRepository.findByUserUuid(userId)
                .orElseThrow(() -> new RuntimeException("지갑 없음"));

        // 1. 다시 돈 채워주기 (refund 메서드가 없다면 charge 사용 가능)
        // Entity에 refund 메서드가 없다면 charge(amount)와 로직이 같습니다.
        wallet.charge(amount);

        // 2. 히스토리 저장 (환불 기록)
        saveHistory(wallet, amount, "REFUND");

        // 3. 기존 로그 유지
        log.info("↩️ 포인트 환불(롤백) 완료: 사용자={}, 환불액={}", userId, amount);
    }

    /**
     * Persist a point transaction record for the given PointMaster.
     *
     * Creates and saves a PointHistory entry that records the pointId from the provided
     * master, the amount, the transaction type, and the current timestamp.
     *
     * @param master the PointMaster whose pointId will be recorded in history
     * @param amount the amount of points for the transaction
     * @param type   the transaction type (e.g., "CHARGE", "USE", "REFUND")
     */
    private void saveHistory(PointMaster master, Long amount, String type) {
        PointHistory history = PointHistory.builder()
                .pointId(master.getPointId())
                .amount(amount)
                .txType(type) // CHARGE, USE, REFUND
                .createdAt(LocalDateTime.now())
                .build();

        pointHistoryRepository.save(history);
    }
}