# =========================
# Stage 1: Build (optimized caching)
# =========================
FROM gradle:8.14.3-jdk24-alpine AS build

WORKDIR /app

# Copy only dependency descriptors first (cache layer)
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle/ gradle/

# Pre-download dependencies (cache optimization)
RUN gradle dependencies --no-daemon

# Copy source
COPY src/ src/

# Build (skip tests for faster CI, run separately)
RUN gradle clean build -x test --no-daemon


# =========================
# Stage 2: Runtime (minimal + secure)
# =========================
FROM eclipse-temurin:24-jre-noble

LABEL org.opencontainers.image.title="payroll-service"
LABEL org.opencontainers.image.version="1.0.0"
LABEL org.opencontainers.image.description="Payroll Management System Backend"
LABEL org.opencontainers.image.authors="payroll-team@techcorp.com"

# Create non-root user (security best practice)
RUN groupadd -r payroll && useradd -r -g payroll payroll

WORKDIR /app

# Install only required package (curl for healthcheck)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Copy built JAR
COPY --from=build /app/build/libs/*.jar app.jar

# Ownership
RUN chown payroll:payroll app.jar

USER payroll

# Expose app port
EXPOSE 8080

# Healthcheck (Spring Boot Actuator required)
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM tuning (container-aware)
ENV JAVA_OPTS="\
-XX:+UseContainerSupport \
-XX:MaxRAMPercentage=75.0 \
-XX:+UseG1GC \
-XX:+ExitOnOutOfMemoryError \
-Djava.security.egd=file:/dev/./urandom"

# Graceful shutdown support
STOPSIGNAL SIGTERM

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]