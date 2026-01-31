# 하이브리드 클라우드 배포 가이드

## 시스템 구조
```
[온프레미스]
├── core-user-service (JAR 배포)
└── core-payment-service (JAR 배포)

[클라우드 - EC2 4대]
├── EC2-1: K8s Master Node
├── EC2-2: Kafka 전용 노드
├── EC2-3: K8s Worker Node (쿠폰 서비스 Pod)
└── EC2-4: K8s Worker Node (쿠폰 서비스 Pod)
```

---

## 📦 1. 온프레미스 배포 (JAR)

### 1-1. User Service 배포

```bash
# 1. JAR 빌드
cd on-premise/core-user-service
./gradlew clean build -x test

# 2. 배포 디렉토리 생성
sudo mkdir -p /opt/core-user-service

# 3. JAR 파일 복사
sudo cp build/libs/core-user-service-*.jar /opt/core-user-service/core-user-service.jar

# 4. systemd 서비스 파일 복사
sudo cp core-user-service.service /etc/systemd/system/

# 5. 서비스 시작
sudo systemctl daemon-reload
sudo systemctl enable core-user-service
sudo systemctl start core-user-service

# 6. 상태 확인
sudo systemctl status core-user-service
sudo journalctl -u core-user-service -f
```

### 1-2. Payment Service 배포

```bash
# User Service와 동일한 과정
cd on-premise/core-payment-service
./gradlew clean build -x test
sudo mkdir -p /opt/core-payment-service
sudo cp build/libs/core-payment-service-*.jar /opt/core-payment-service/core-payment-service.jar
sudo cp core-payment-service.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable core-payment-service
sudo systemctl start core-payment-service
sudo systemctl status core-payment-service
```

---

## 🚀 2. 클라우드 배포 (K8s + Docker)

### 사전 준비

#### Kafka EC2 설정
```bash
# Kafka EC2에 접속
ssh user@kafka-ec2-ip

# Kafka 설치 및 실행 (이미 되어 있다면 스킵)
# ... Kafka 설정 ...

# Topic 생성
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic coupon_issue \
  --partitions 3 \
  --replication-factor 1
```

#### Redis 설정
```bash
# Redis가 설치된 서버에서
sudo systemctl start redis
sudo systemctl enable redis

# Redis 연결 테스트
redis-cli ping
```

### 2-1. Docker 이미지 빌드 및 푸시

```bash
# 1. 쿠폰 서비스 빌드
cd cloud-services/msa-coupon-service

# 2. Docker 이미지 빌드
docker build -t your-registry/coupon-service:v1.0.0 .

# 3. Docker Registry에 푸시 (ECR, Docker Hub 등)
# ECR 예시:
aws ecr get-login-password --region ap-northeast-2 | \
  docker login --username AWS --password-stdin your-ecr-url

docker tag coupon-service:v1.0.0 your-ecr-url/coupon-service:v1.0.0
docker push your-ecr-url/coupon-service:v1.0.0

# Docker Hub 예시:
docker login
docker push your-registry/coupon-service:v1.0.0
```

### 2-2. K8s 클러스터 배포

```bash
# K8s Master 노드에 접속
ssh user@master-node-ip

# 1. ConfigMap & Secret 생성 (환경에 맞게 수정)
kubectl create secret generic db-secret \
  --from-literal=username=root \
  --from-literal=password=your-db-password

# ConfigMap 수정 후 적용
vi k8s/configmap-secret.yaml
# Redis, Kafka, MySQL 호스트 정보 수정
kubectl apply -f k8s/configmap-secret.yaml

# 2. Deployment 배포
vi k8s/deployment.yaml
# image 경로를 실제 레지스트리 주소로 수정
# 환경 변수 값들을 실제 값으로 수정
kubectl apply -f k8s/deployment.yaml

# 3. Service 생성
kubectl apply -f k8s/service.yaml

# 4. HPA 설정
# Metrics Server가 설치되어 있어야 함
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
kubectl apply -f k8s/hpa.yaml

# 5. 배포 상태 확인
kubectl get pods -w
kubectl get deployments
kubectl get services
kubectl get hpa
```

### 2-3. 무중단 배포 (Rolling Update)

```bash
# 새 버전 이미지 빌드 및 푸시
docker build -t your-registry/coupon-service:v1.0.1 .
docker push your-registry/coupon-service:v1.0.1

# 이미지 업데이트 (자동으로 Rolling Update 진행)
kubectl set image deployment/coupon-service \
  coupon-service=your-registry/coupon-service:v1.0.1

# 배포 진행 상황 확인
kubectl rollout status deployment/coupon-service

# 롤백 (문제 발생 시)
kubectl rollout undo deployment/coupon-service
```

---

## 🔍 3. 모니터링 및 확인

### Pod 상태 확인
```bash
kubectl get pods -o wide
kubectl describe pod <pod-name>
kubectl logs -f <pod-name>
```

### HPA 동작 확인
```bash
kubectl get hpa
kubectl describe hpa coupon-service-hpa

# 부하 테스트
kubectl run -it --rm load-generator --image=busybox --restart=Never -- /bin/sh
# Pod 내부에서:
while true; do wget -q -O- http://coupon-service:8082/api/coupons; done
```

### 서비스 접근 확인
```bash
# 클러스터 외부에서 접근 (NodePort)
curl http://worker-node-ip:30082/actuator/health

# 클러스터 내부에서 접근
kubectl run -it --rm debug --image=curlimages/curl --restart=Never -- \
  curl http://coupon-service:8082/actuator/health
```

---

## 🔧 4. 트러블슈팅

### Pod가 시작되지 않을 때
```bash
kubectl describe pod <pod-name>
kubectl logs <pod-name>

# 일반적인 원인:
# 1. 이미지를 pull 할 수 없음 → imagePullSecrets 확인
# 2. 환경 변수 오류 → ConfigMap/Secret 확인
# 3. 헬스체크 실패 → Redis/Kafka/MySQL 연결 확인
```

### HPA가 동작하지 않을 때
```bash
# Metrics Server 확인
kubectl get deployment metrics-server -n kube-system
kubectl top nodes
kubectl top pods

# Metrics Server가 없으면 설치
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

### 온프레미스 서비스 문제
```bash
# 서비스 상태 확인
sudo systemctl status core-user-service

# 로그 확인
sudo journalctl -u core-user-service -f

# 서비스 재시작
sudo systemctl restart core-user-service
```

---

## 📊 5. 확인해야 할 주요 포인트

### Redis 연결
```bash
# 쿠폰 서비스 Pod에서 Redis 연결 테스트
kubectl exec -it <pod-name> -- /bin/sh
# telnet redis-host 6379
```

### Kafka 연결
```bash
# Kafka 토픽 메시지 확인
kafka-console-consumer.sh \
  --bootstrap-server kafka-host:9092 \
  --topic coupon_issue \
  --from-beginning
```

### MySQL 연결
```bash
# 쿠폰 DB 확인
mysql -h mysql-host -u root -p
use coupon_db;
show tables;
select * from coupons limit 10;
```

---

## 🎯 6. 최종 체크리스트

- [ ] Kafka 토픽 생성 확인
- [ ] Redis 연결 확인
- [ ] MySQL DB 생성 및 권한 확인
- [ ] Docker 이미지 빌드 및 푸시 완료
- [ ] ConfigMap/Secret에 실제 값 입력
- [ ] Deployment의 image 경로 수정
- [ ] K8s 리소스 모두 배포 (deployment, service, hpa)
- [ ] Metrics Server 설치 확인
- [ ] Pod 정상 실행 확인 (Running 상태)
- [ ] HPA 동작 확인
- [ ] 온프레미스 서비스 정상 실행 확인
- [ ] 전체 플로우 테스트 (회원가입 → 쿠폰발급)
