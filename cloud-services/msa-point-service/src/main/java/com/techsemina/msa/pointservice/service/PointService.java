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
     * [기능 1] 포인트 적립 (Upsert)
     * - 이미 있는 유저면? -> 기존 금액 + 충전 금액 (오류 안 남!)
     * - 없는 유저면? -> 새로 생성
     * - 그리고 히스토리에 기록!
     */
    public PointMaster chargePoint(String userUuid, long amount) {
        // 1. 유저 조회 (없으면 새로 생성 - 0원으로 초기화)
        PointMaster pointMaster = pointRepository.findByUserUuidWithLock(userUuid)
                .orElseGet(() -> pointRepository.save(new PointMaster(userUuid, 0)));

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
     * [기능 2] 포인트 사용 (결제)
     * - 비관적 락(Lock)을 걸어서 동시성 이슈 방지
     * - 잔액 체크 후 차감
     * - 히스토리 저장 추가
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
     * [기능 3] 보상 트랜잭션 (포인트 환불/롤백)
     * - 온프레미스(은행) 쪽에서 에러났을 때 호출됨
     * - 히스토리 저장 추가
     */
    public void refund(String userId, Long amount) {
        PointMaster wallet = pointRepository.findByUserUuidWithLock(userId)
                .orElseThrow(() -> new RuntimeException("지갑 없음"));

        // 1. 다시 돈 채워주기 (refund 메서드가 없다면 charge 사용 가능)
        // Entity에 refund 메서드가 없다면 charge(amount)와 로직이 같습니다.
        wallet.charge(amount);

        // 2. 히스토리 저장 (환불 기록)
        saveHistory(wallet, amount, "REFUND");

        // 3. 기존 로그 유지
        log.info("↩️ 포인트 환불(롤백) 완료: 사용자={}, 환불액={}", userId, amount);
    }

    // [내부 메서드] 히스토리 저장 로직 공통화 (중복 제거)
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