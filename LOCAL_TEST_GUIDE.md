# 로컬 테스트 가이드

로컬 환경에서 전체 시스템(온프레미스 + 클라우드)을 테스트하는 방법입니다.

---

## 📋 사전 준비

### 필수 설치 프로그램
- ✅ Java 17
- ✅ Docker Desktop (최소 8GB 메모리 권장)
- ✅ IntelliJ IDEA (또는 다른 IDE)

---

## 🗄️ 데이터베이스 구조

이 프로젝트는 **하이브리드 클라우드**를 반영하여 서비스마다 다른 DB를 사용합니다:

| 서비스 | DB 종류 | 포트 | 용도 |
|--------|---------|------|------|
| **core-user-service** | PostgreSQL | 5432 | 온프레미스 (개인정보 보관) |
| **core-payment-service** | Oracle | 1521 | 온프레미스 (결제 정보) |
| **msa-coupon-service** | MySQL | 3306 | 클라우드 (쿠폰 발급) |

---

## 🚀 간단 실행

### Step 1: 전체 인프라 실행

```bash
# 쿠폰 서비스 디렉토리로 이동
cd cloud-services/msa-coupon-service

# Docker Compose로 전체 인프라 실행
# (Redis, Kafka, MySQL, PostgreSQL, Oracle)
docker-compose up -d

# 실행 확인 (6개 컨테이너 모두 Up 상태여야 함)
docker-compose ps
```

**예상 결과:**
```
NAME              IMAGE                       STATUS
local-redis       redis:alpine                Up
local-zookeeper   confluentinc/cp-zookeeper   Up
local-kafka       confluentinc/cp-kafka       Up
local-mysql       mysql:8.0                   Up
local-postgres    postgres:15-alpine          Up
local-oracle      gvenzl/oracle-xe:21-slim    Up
```

**⚠️ 주의:** Oracle은 시작하는데 **1-2분** 정도 걸릴 수 있습니다!

### Step 2: Oracle 준비 대기 (중요!)

```bash
# Oracle 로그 확인 (DATABASE IS READY 메시지 확인)
docker logs -f local-oracle

# 다음 메시지가 나올 때까지 대기:
# DATABASE IS READY TO USE!
```

### Step 3: Kafka 토픽 생성

```bash
# Kafka 컨테이너 접속
docker exec -it local-kafka bash

# 토픽 생성
kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic coupon_issue \
  --partitions 3 \
  --replication-factor 1

# 토픽 확인
kafka-topics --list --bootstrap-server localhost:9092

# 나가기
exit
```

### Step 4: Redis 초기 재고 설정

```bash
# Redis에 쿠폰 재고 100개 설정
docker exec -it local-redis redis-cli SET coupon:count 100

# 확인
docker exec -it local-redis redis-cli GET coupon:count
```

### Step 5: Spring Boot 애플리케이션 실행

IntelliJ에서 **순서대로** 실행:

**1. User Service 실행 (PostgreSQL 사용)**
- `on-premise/core-user-service/src/main/java/com/fisa/core_user_service/CoreUserServiceApplication.java`
- 우클릭 → "Run"
- ✅ 포트 8080에서 실행 확인

**2. Payment Service 실행 (Oracle 사용)**
- `on-premise/core-payment-service/src/main/java/com/fisa/core_payment_service/CorePaymentServiceApplication.java`
- 우클릭 → "Run"
- ✅ 포트 8081에서 실행 확인

**3. Coupon Service 실행 (MySQL 사용)**
- `cloud-services/msa-coupon-service/src/main/java/fisa/coupon/CouponApplication.java`
- 우클릭 → "Run"
- ✅ 포트 8082에서 실행 확인

### Step 6: 헬스 체크

```bash
# User 서비스 (PostgreSQL)
curl http://localhost:8080/actuator/health

# Payment 서비스 (Oracle)
curl http://localhost:8081/actuator/health

# Coupon 서비스 (MySQL)
curl http://localhost:8082/actuator/health
```

모두 `{"status":"UP"}`이 나와야 합니다!

---

## 🧪 테스트

### 전체 플로우 테스트

**1. 회원가입 (PostgreSQL에 저장)**
```bash
curl -X POST http://localhost:8080/api/core/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "realName": "홍길동",
    "residentNo": "900101-1234567"
  }'
```

**응답 예시:**
```json
{
  "userUuid": "550e8400-e29b-41d4-a716-446655440000",
  "maskedName": "홍*동",
  "maskedResidentNo": "900101-1******"
}
```

**2. 쿠폰 발급 (MySQL에 저장)**
```bash
# 위에서 받은 userUuid 사용
curl -X POST "http://localhost:8082/api/coupons/issue?userUuid=550e8400-e29b-41d4-a716-446655440000"
```

**3. 각 DB 확인**

**PostgreSQL (User 정보):**
```bash
docker exec -it local-postgres psql -U postgres -d user_db

# 사용자 확인
SELECT * FROM tb_real_user;

# 나가기
\q
```

**Oracle (Payment 정보):**
```bash
docker exec -it local-oracle sqlplus system/password@XEPDB1

# 테이블 확인
SELECT table_name FROM user_tables;

# 나가기
EXIT;
```

**MySQL (Coupon 정보):**
```bash
docker exec -it local-mysql mysql -uroot -ppassword coupon_db

# 쿠폰 확인
SELECT * FROM coupons;

# 나가기
exit
```

### 동시성 테스트

IntelliJ에서:
1. `CouponServiceTest.java` 열기
2. `concurrencyTest()` 메서드 ▶️ 클릭
3. 1000명 동시 요청 → 100개만 발급 확인

---

## 🔍 트러블슈팅

### 1. Oracle 시작 안 됨

**증상:** `ORA-12541: 접속할 수 없습니다`

**해결:**
```bash
# Oracle 로그 확인
docker logs local-oracle

# Oracle 컨테이너 재시작
docker-compose restart oracle

# 1-2분 대기 후 다시 확인
docker logs local-oracle | grep "DATABASE IS READY"
```

### 2. PostgreSQL 연결 실패

**증상:** `Connection refused` or `FATAL: password authentication failed`

**해결:**
```bash
# PostgreSQL 재시작
docker-compose restart postgres

# 로그 확인
docker logs local-postgres
```

### 3. Docker Desktop 메모리 부족

**증상:** 컨테이너가 자주 죽거나 느림

**해결:**
- Docker Desktop 설정 → Resources
- Memory를 최소 8GB로 증가

### 4. 포트 충돌

```bash
# Windows
netstat -ano | findstr :1521
netstat -ano | findstr :5432
netstat -ano | findstr :3306

# Mac/Linux
lsof -i :1521
lsof -i :5432
lsof -i :3306
```

---

## 🛑 종료

```bash
# Spring Boot 애플리케이션 종료
# IntelliJ에서 Stop 버튼 클릭

# Docker 인프라 종료
cd cloud-services/msa-coupon-service
docker-compose down

# 데이터까지 삭제 (완전 초기화)
docker-compose down -v
```

---

## ✅ 빠른 체크리스트

실행 전:
- [ ] Docker Desktop 실행 중 (메모리 8GB 이상)
- [ ] Java 17 설치 확인
- [ ] 포트 확인: 8080, 8081, 8082, 1521, 3306, 5432, 6379, 9092

실행 순서:
1. [ ] `docker-compose up -d`
2. [ ] Oracle 준비 대기 (1-2분)
3. [ ] Kafka 토픽 생성
4. [ ] Redis 재고 설정
5. [ ] User Service 실행 (8080)
6. [ ] Payment Service 실행 (8081)
7. [ ] Coupon Service 실행 (8082)
8. [ ] 헬스체크 확인
9. [ ] 전체 플로우 테스트

---

## 📊 데이터 흐름 확인

```
온프레미스 (중요 정보 보관)
├── PostgreSQL (User) - 개인정보 (암호화)
└── Oracle (Payment) - 결제 정보

         ↓ userUuid만 전송

클라우드 (쿠폰 발급)
└── MySQL (Coupon) - 쿠폰 정보
    └── Redis - 재고 관리
    └── Kafka - 비동기 처리
```

이제 완벽한 하이브리드 클라우드 환경에서 테스트할 수 있습니다! 🎉
