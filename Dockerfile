# Multi-stage Dockerfile for NearYou ID Server

# Stage 1: Build
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy Gradle files
COPY gradle gradle
COPY gradlew .
COPY gradle.properties .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Copy shared module
COPY shared shared

# Copy server module
COPY server server

# Build the application
RUN ./gradlew :server:shadowJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy the built JAR from build stage
COPY --from=build /app/server/build/libs/server-all.jar app.jar

# Create non-root user
RUN addgroup -S nearyou && adduser -S nearyou -G nearyou
USER nearyou

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Run the application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

