# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=builder /build/target/*-SNAPSHOT.jar app.jar
EXPOSE 8761
HEALTHCHECK --interval=30s --timeout=5s --start-period=50s --retries=3 \
  CMD curl -sf http://127.0.0.1:8761/actuator/health > /dev/null || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
