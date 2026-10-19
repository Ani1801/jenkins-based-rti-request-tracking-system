# =========================================================================
# Multi-Stage Dockerfile for RTI Request Tracking System
# Stage 1: Build & package Spring Boot application with Maven
# Stage 2: Minimal, secure Java 17 runtime container
# =========================================================================

# --- Stage 1: Builder ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /workspace

# Copy POM and resolve dependencies for layer caching
COPY pom.xml .
COPY src ./src

# Package application JAR without re-running unit tests (pre-verified in pipeline)
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime ---
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="College DevOps Team" \
      project="rti-request-tracking-system" \
      version="1.0.0"

WORKDIR /app

# Create unprivileged system group and user
RUN addgroup -S rtigroup && adduser -S rtiuser -G rtigroup

# Copy compiled JAR from builder stage
COPY --from=builder --chown=rtiuser:rtigroup /workspace/target/rti-request-tracking-system-1.0.0-SNAPSHOT.jar app.jar

# Run as non-root user
USER rtiuser

# Expose internal application HTTP port
EXPOSE 8080

# Configure JVM container options and environment variables
ENV SERVER_PORT=8080 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
