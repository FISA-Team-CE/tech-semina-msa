# 우리FISA 1차 기술 세미나: 뱅킹 시스템에서의 MSA 도입

> 진행 기간 : 2026.01.21 ~ 2026.02.05


## 👩🏻‍💻 팀원 소개

| <img src="https://avatars.githubusercontent.com/u/121941036?v=4" width="150" height="150"/> | <img src="https://avatars.githubusercontent.com/u/118096607?v=4" width="150" height="150"/> | <img src="https://avatars.githubusercontent.com/u/80169727?v=4" width="150" height="150"/> | <img src="https://avatars.githubusercontent.com/u/73377952?v=4" width="150" height="150"/> |
| :-----------------------------------------------------------------------------------------: | :-----------------------------------------------------------------------------------------: | :-----------------------------------------------------------------------------------------: |  :-----------------------------------------------------------------------------------------: | 
|                 류승환<br/>[@Federico-15](https://github.com/Federico-15)                 |                       서가영<br/>[@caminobelllo](https://github.com/caminobelllo)                       |                유동균<br/>[@dbehdrbs0806](https://github.com/dbehdrbs0806)                |                백주연<br/>[@juyeonbaeck](https://github.com/juyeonbaeck)                  


<br>

## ⚙️ 기술 스택
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) <br>
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka)
![Oracle](https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white) 
![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white) <br>
![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Kubernetes](https://img.shields.io/badge/kubernetes-%23326ce5.svg?style=for-the-badge&logo=kubernetes&logoColor=white)
![Nginx](https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white)
![Tailscale](https://img.shields.io/badge/Tailscale-000000?style=for-the-badge) <br>
![Notion](https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)
![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)

<br>
 
## 📊 ERD
<img width="1874" height="590" alt="image" src="https://github.com/user-attachments/assets/0d3f3592-df07-4395-8505-425223625bf2" />

<br>
<br>

## 🌐 아키텍처 구성
### 전략
보안과 규제 준수가 중요한 뱅킹 시스템을 위해 온프레미스와 클라우드를 결합한 하이브리드 클라우드 채택

### 특징
1. 서비스 단위 스케일링을 통한 트래픽 제어
2. API Gateway를 중심으로 라우팅 구성
3. 분산 환경을 고려한 DB 분리 및 트랜잭션 이벤트 흐름 설계
<img width="6115" height="4087" alt="fisa-msa pdf" src="https://github.com/user-attachments/assets/e535b11a-8c19-4fc5-bee2-638b40883f73" />

<br>
<br>

## 🍎 서비스별 기능

### 1️⃣ Core User Service
> 계정계 : 사용자 정보 관리

**1. 회원 등록**
 - 클라이언트로부터 사용자 정보를 받아 계정계 회원으로 등록
 - 등록된 회원은 채널계에서 로그인을 통해 기능 사용 가능
 - 중복 가입 여부 체크 후 저장
 - 등록이 완료되면 고유 식별자인 UUID를 발급하며, 이는 MSA 시스템에서 유저 식별을 위한 global key로 사용

**2. 금융 실명 관리 및 마스킹 처리**
 - 금융감독원에서 발행한 <금융분야 가명 · 익명처리 안내서> 기법에 따라 고객의 실명, 주민등록번호 등 민감 정보를 AES-256 등으로 암호화하여 저장
 - 외부 채널계에는 실명 정보 노출하지 않으며, 매핑된 userUuid만 반환해 가명 처리 수행
 - 정보 조회 및 로깅 시에 마스킹 처리 후 반환

<br>

### 2️⃣ Core Payment Service
> 계정계 : 결제 및 트랜잭션

**1. 계좌 관리 및 자산 처리**
 - 계좌 개설 : 사용자 고유 ID(userUuid)와 매핑되는 계좌를 생성. 중복 계좌 생성을 방지
 - 입금 : 계좌 잔액을 증가. 트래픽 격리를 위해 주로 Kafka Consumer를 통해 비동기로 호출
 - 출금 : 계좌 잔액을 차감. 잔액 부족 시 예외를 발생시켜 트랜잭션을 롤백

**2. 이벤트 기반 아키텍처**
 > Kafka : 채널계에서의 트래픽으로부터 코어 뱅킹 시스템을 보호하기 위한 버퍼 역할
 - 입금 트래픽 격리 : 채널 서비스가 유효한 요청만 kafka로 전송하면, core service는 메시지를 하나씩 꺼내 core Oracle DB에 반영 
 - 쿠폰 발급 이력 저장 : 선착순 쿠폰 발급 정보를 수신해 데이터베이스에 발급된 쿠폰 정보를 영구 저장

**3. 분산 트랜잭션 전략**
 - AWS 클라우드와 온프레미스 Core Payment 간의 물리적으로 분리된 DB 데이터 정합성을 보장

<br>

### 3️⃣ Channel User Service
> 채널계 : 클라이언트와 계정계 사이를 중계

**1. 인증 및 인가**
 - 회원가입 : 채널 계정 생성 & Core User Service 실명 인증 연동
 - 로그인 : ID/PW 검증 후 JWT 토큰 발급

**2. 뱅킹 서비스**

<img width="656" height="672" align="center" src="https://github.com/user-attachments/assets/e49575cb-b5a4-4ffe-b2e4-535e305f00f0" />
<br>

 - 계좌 개설 : Core Payment Service에 계좌 생성 요청 (동기 REST 처리 방식 사용)
 - 입금 : Kafka를 통한 입금 요청 발행 (비동기 Event 처리 방식 사용)
 - 출금 : Feign Clien를 통해 Core Payment Service에 출금 요청 (동기 REST 처리 방식 사용)

<br>

### 4️⃣ Point Service
> 채널계 : 포인트 결제 및 분산 트랜잭션

**1. 복합 결제 (포인트 + 현금)**
 - 사용자가 [ 포인트 + 현금 ] 복합 결제 요청 시, MySQL DB에서 포인트 선차감
 - 차감 성공 시 kafka에 payment_request 이벤트를 발행해 Core payment Service에 현금 출금을 요청

**2. 보상 트랜잭션**
 > 분산 환경에서의 데이터 결과적 일관성 (Eventual Consistency) 보장
 - Core Service로부터 payment_result: FAIL (잔액 부족) 이벤트를 수신 시, 즉시 롤백 로직 실행
 - 앞에서 차감한 포인트를 다시 더함

**3. 이벤트 기반 처리**
 - Kafka Producer/Consumer를 모두 배치해 Core와의 통신을 비동기 메시지로 처리

<br>

### 5️⃣ Coupon Service
> 채널계 : 트래픽 처리 및 쿠폰 관리

**1. Lua Script 원자성**
 - Single Redis Call : 중복 체크와 재고 차감을 하나의 Lua 스크립트로 묶어 원자적으로 실행
 - Race Condition 방지 : 다수의 동시 요청이 들어와도 정확한 쿠폰 개수만큼만 발급

**2. 비동기 발급**
 - Kafka Producer : 쿠폰 당첨 확정 시 DB Insert 대신 Kafka 토픽에 이벤트 발행 후 즉시 응답
 - ExecutorService : 고정 스레드 풀로 Kafka 전송을 관리해 커넥션 고갈 방지
 - 롤백 로직을 통해 Kafka 장애 시에도 데이터 정합성 유지

**3. 자동 복구 스케줄러**
 - 1분 주기로 실행되어 Redis에는 기록되었으나 DB에 반영되지 않는 쿠폰을 감지해 Kakfa로 재전송
 - 서버 다운 시점의 누락 쿠폰 자동 복구 효과

<br>
<br>

## 🎬 다양한 MSA 시나리오
### 1️⃣ 부하테스트

### 2️⃣ 보상 트랜잭션: SAGA pattern

<br>
<br>

## 🚀 트러블 슈팅
### 1️⃣ 스레드 풀 최적화
1. 문제
 - 부하 발생 시 응답 속도의 표준 편차가 커서, 사용자 경험이 일정하지 않은 현상 발견

2. 도입 배경
 - 부하 테스트 중 간헐적인 응답 지연 발생
 - Kafka 메시지 전송 시 동기 처리로 인한 병목 현상
 - 매 요청마다 스레드 생성/제거로 인한 리소스 낭비

3. 적용 기술
 - 고정 크기 스레드 풀 구현 (크기: 20)
 - CompletableFuture를 활용한 Kafka 비동기 메시지 전송
 - 스레드 재사용을 통한 시스템 리소스 효율화

4. 최종 결과
 <img width="697" height="374" alt="스크린샷 2026-02-04 오후 11 44 17" src="https://github.com/user-attachments/assets/39572335-02f1-4f6f-b3de-a8c9b318b36c" />

 - 평균 응답 속도 23배 향상 (3MS 달성)
 - 대규모 트래픽 상황에서도 안정적인 서비스 가능

<br>

### 2️⃣ CoreDNS와 /etc/resolv.conf
1. 문제
 - CoreDNS가 /etc/resolv.conf 파일의 잘못된 주소를 읽고 참조하여 올바른 DNS를 찾지 못하는 문제 발생

2. 해결
 <img width="400" height="520" alt="carbon (1)" src="https://github.com/user-attachments/assets/adb7b815-9ddb-43dd-bd76-4577c736c814" />

 - CoreDNS의 설정 파일의 의존성을 제거하고 외부 공용DNS로 강제통신 하도록 수정하여  조회 실패 문제를 해결

<br>

### 3️⃣ 노드 IP 인식 문제
1. 문제
 - 기존 VPC 상의 IP 172.31.x.x 를 통해 Pod끼리의 통신이 가능하나, Tailscale 사용 시 tailscale0 인터페이스를 생성
 - 따라서 kubelet이 자동으로 Tailscale의 100.xx  IP를 node IP로 인식해 꼬임 발생

2. 해결
 <img width="520" height="220" alt="carbon" src="https://github.com/user-attachments/assets/05d0b155-26ff-450d-9265-d2476174f0d2" />

   
