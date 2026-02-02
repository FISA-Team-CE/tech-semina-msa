# 1. 자바 실행 환경(JRE) 선택
FROM openjdk:17-jdk-slim

# 2. 빌드된 jar 파일을 컨테이너 내부로 복사
# Gradle 빌드 시 생성되는 파일 경로를 지정합니다.
COPY build/libs/*.jar app.jar

# 3. 컨테이너가 시작될 때 실행할 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]