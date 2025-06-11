# Sử dụng image OpenJDK 17 làm base
FROM openjdk:17-jdk-slim

# Thiết lập thư mục làm việc
WORKDIR /app

# Sao chép file Maven và cấu hình
COPY pom.xml .
COPY .mvn/ .mvn
COPY mvnw mvnw.cmd

# Cài đặt dependencies (tận dụng cache)
RUN ./mvnw dependency:go-offline

# Sao chép mã nguồn
COPY src ./src

# Build ứng dụng
RUN ./mvnw clean package -DskipTests

# Expose cổng 8080
EXPOSE 8080

# Chạy ứng dụng
CMD ["java", "-jar", "target/Health-0.0.1-SNAPSHOT.jar"]