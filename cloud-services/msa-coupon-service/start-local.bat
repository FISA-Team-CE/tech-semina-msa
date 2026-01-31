@echo off
echo ========================================
echo Starting Local Test Environment
echo ========================================
echo.

echo [1/5] Starting Docker infrastructure...
echo   - Redis, Kafka, MySQL, PostgreSQL, Oracle
docker-compose up -d

echo.
echo [2/5] Waiting for databases to be ready (60 seconds)...
echo   Oracle takes 1-2 minutes to start...
timeout /t 60 /nobreak >nul

echo.
echo [3/5] Creating Kafka topic...
docker exec local-kafka kafka-topics --create --bootstrap-server localhost:9092 --topic coupon_issue --partitions 3 --replication-factor 1 --if-not-exists

echo.
echo [4/5] Setting initial coupon count...
docker exec local-redis redis-cli SET coupon:count 100

echo.
echo [5/5] Checking Oracle status...
docker exec local-oracle bash -c "echo 'SELECT 1 FROM DUAL;' | sqlplus -s system/password@XEPDB1" >nul 2>&1
if %errorlevel%==0 (
    echo   Oracle is ready!
) else (
    echo   Oracle is still starting... wait 1-2 minutes more
)

echo.
echo ========================================
echo Environment Ready!
echo ========================================
echo.
echo Services:
echo   - Redis:      localhost:6379
echo   - Kafka:      localhost:9092
echo   - MySQL:      localhost:3306  (Coupon Service)
echo   - PostgreSQL: localhost:5432  (User Service)
echo   - Oracle:     localhost:1521  (Payment Service)
echo.
echo Next Steps:
echo   1. Run CoreUserServiceApplication (Port 8080)
echo   2. Run CorePaymentServiceApplication (Port 8081)
echo   3. Run CouponApplication (Port 8082)
echo   4. Test: curl http://localhost:8082/actuator/health
echo.
pause
