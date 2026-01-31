# Tech Seminar - MSA Project

This repository contains both on-premise and cloud-based microservices.

## ?? Project Structure
```n tech-semina-msa/
 ├── on-premise/              # On-Premise Services
 │   ├── core-user-service/   # User management (PostgreSQL)
 │   └── core-payment-service/ # Payment & Coupon (Oracle DB)
 ├── cloud-services/          # Cloud MSA Services
 │   └── msa-coupon-service/  # Coupon service (MySQL, Redis, Kafka)
 └── README.md
```n
## ??? Architecture

### On-Premise Services (온프레미스)
- **core-user-service** (Port: 8080)
  - Customer data management with encryption
  - Database: PostgreSQL
  
- **core-payment-service** (Port: 8081)
  - Payment processing and coupon issuance tracking
  - Database: Oracle DB
  - Kafka Consumer: coupon_issue topic

### Cloud Services (클라우드)
- **msa-coupon-service** (Port: 8082)
  - High-performance coupon issuance
  - Redis-based stock management
  - Kafka Producer: coupon_issue topic
  - Database: MySQL

## ?? Service Communication
```n[User Request]
     ↓
msa-coupon-service (Cloud)
     ↓ Redis check & stock decrease
     ↓
   Kafka (coupon_issue topic)
     ↓
  ┌──────────────────┐
  ↓                  ↓
msa-coupon-service  core-payment-service
(MySQL)             (Oracle DB - On-Premise)
```n
## ?? Getting Started

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Kafka, Redis, MySQL, PostgreSQL, Oracle DB

### Running Services
```bash
# 1. Start infrastructure (Redis, Kafka, MySQL)
cd cloud-services/msa-coupon-service
docker-compose up -d

# 2. Run on-premise services
cd on-premise/core-user-service
./gradlew bootRun

cd on-premise/core-payment-service
./gradlew bootRun

# 3. Run cloud service
cd cloud-services/msa-coupon-service
./gradlew bootRun
```n
## ?? API Endpoints

### msa-coupon-service (Port: 8082)
- POST /coupons/issue?userUuid={uuid} - Issue a coupon

## ?? Configuration

Each service has its own pplication.properties with different ports:
- core-user-service: 8080
- core-payment-service: 8081
- msa-coupon-service: 8082

