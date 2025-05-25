# Use Maven + JDK 22 for building (instead of just JDK)
FROM maven:3.9.6-eclipse-temurin-22-jdk AS build

# Set working directory
WORKDIR /app

# Copy POM first (for better layer caching)
COPY pom.xml .
# Download dependencies (cached unless POM changes)
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src
# Build the app (skip tests for faster build)
RUN mvn package -DskipTests

# Final stage: Only copy the JAR (smaller image)
FROM openjdk:22-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose port (adjust if needed)
EXPOSE 8080
# Run the app
CMD ["java", "-jar", "app.jar"]