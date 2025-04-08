# syntax=docker/dockerfile:1.4

FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Copy gradle files first for better caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make the gradlew script executable
RUN chmod +x gradlew

# Download dependencies
RUN ./gradlew dependencies

# Copy source code
COPY src src

# Build the application
RUN ./gradlew clean build -x test -x check

# Use slim JRE for the runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR file
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# Configure Java for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0"

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
