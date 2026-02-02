#!/bin/bash

# --- 설정 (Config) ---
BASE_URL="http://localhost:8080"
TOTAL_COUPONS=100
TOTAL_USERS=150
LOG_FILE="test_result.log"

# 색상 코드
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}==================================================${NC}"
echo -e "${GREEN}   🚀 [MAC용] 선착순 쿠폰 시스템 스트레스 테스트   ${NC}"
echo -e "${GREEN}==================================================${NC}"

# 1. 초기화 (Reset)
echo -e "\n[Step 1] Redis 쿠폰 수량을 ${TOTAL_COUPONS}개로 초기화합니다..."
# 연결 실패 시 에러 내용을 보기 위해 -v 옵션은 껐으나, 실패 시 메시지 출력
RESET_RES=$(curl -s -X POST "${BASE_URL}/coupons/reset?count=${TOTAL_COUPONS}")

if [ -z "$RESET_RES" ]; then
    echo -e "${RED}🚨 [Error] 서버 응답이 없습니다! 서버가 켜져 있는지 확인하세요.${NC}"
    echo -e "👉 터미널에서 'curl -v -X POST http://localhost:8080/coupons/reset?count=100'을 실행해서 에러 원인을 확인해보세요."
    exit 1
else
    echo "👉 응답: $RESET_RES"
fi

# 로그 파일 초기화
rm -f $LOG_FILE
touch $LOG_FILE

# 2. 동시 요청 실행
echo -e "\n[Step 2] ${TOTAL_USERS}명의 유저가 동시에 발급을 요청합니다!"

# Mac 호환 시간 측정
START_TIME=$(python3 -c 'import time; print(int(time.time() * 1000))')

for ((i=1; i<=TOTAL_USERS; i++)); do
    (
        USER_ID="user-uuid-$i"
        # --fail 옵션을 쓰면 400/500 에러 시 curl이 빈 값을 반환함
        response=$(curl -s -X POST "${BASE_URL}/coupons/issue?userUuid=${USER_ID}")
        echo "$response" >> $LOG_FILE
    ) &
done

wait

END_TIME=$(python3 -c 'import time; print(int(time.time() * 1000))')
DURATION=$((END_TIME - START_TIME))

echo -e "\n✅ 모든 요청 처리 완료! (소요시간: ${DURATION}ms)"

# 3. 결과 분석
SUCCESS_COUNT=$(grep -c "당첨" $LOG_FILE)
FAIL_COUNT=$(grep -c "마감" $LOG_FILE)
LOCK_FAIL=$(grep -c "Lock Timeout" $LOG_FILE)
ERROR_COUNT=$(grep -c "Error" $LOG_FILE)

echo -e "\n[Step 3] 결과 분석"
echo -e "${GREEN}--------------------------------------------------${NC}"
echo -e "   총 요청 수 : ${TOTAL_USERS}"
echo -e "   🎉 성공 (Kafka) : ${GREEN}${SUCCESS_COUNT}${NC}"
echo -e "   ❌ 실패 (마감)   : ${RED}${FAIL_COUNT}${NC}"
echo -e "   ⏳ 실패 (Timeout): ${RED}${LOCK_FAIL}${NC}"
echo -e "${GREEN}--------------------------------------------------${NC}"

if [ "$SUCCESS_COUNT" -eq 0 ] && [ "$FAIL_COUNT" -eq 0 ]; then
    echo -e "\n${RED}🚨 결과가 이상합니다. 로그 파일($LOG_FILE) 내용을 확인합니다:${NC}"
    head -n 5 $LOG_FILE
fi