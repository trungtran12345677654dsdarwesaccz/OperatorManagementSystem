# Giai đoạn 1: Build app.jar
FROM maven:3.9.5-eclipse-temurin-17 as builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Giai đoạn 2: Chạy app với biến PORT do Render cung cấp
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Sửa ENTRYPOINT để đọc biến môi trường PORT đúng cách
ENTRYPOINT exec java -Dserver.port=${PORT} -jar app.jar
