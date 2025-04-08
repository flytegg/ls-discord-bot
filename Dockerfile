# syntax=docker/dockerfile:1.4
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy Gradle files for better caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test -x check

# Use a lightweight JRE image for runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Configure Java for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0"

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
