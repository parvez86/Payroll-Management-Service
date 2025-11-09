# Build stage
FROM gradle:8.14.3-jdk24-alpine AS build

WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle ./
COPY gradle/ gradle/

# Copy source code
COPY src/ src/

# Build the application
RUN gradle clean build -x test --no-daemon


# Runtime stage
FROM debian:bullseye-slim

LABEL maintainer="payroll-team@techcorp.com"
LABEL version="1.0.0"
LABEL description="Payroll Management System Backend"

# Copy local JDK 24 from build context (must be present in project root as 'jdk-24')
COPY jdk-24 /usr/local/openjdk-24
ENV JAVA_HOME=/usr/local/openjdk-24
ENV PATH="$JAVA_HOME/bin:$PATH"

# Create app user for security
RUN groupadd -r payroll && useradd -r -g payroll payroll

# Set working directory
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the JAR file from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership to app user
RUN chown payroll:payroll app.jar

# Switch to non-root user
USER payroll

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# JVM Options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]