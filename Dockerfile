# Use an official OpenJDK 22 runtime as a parent image
FROM openjdk:22-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and pom.xml
COPY mvnw* pom.xml ./
COPY .mvn .mvn

# Copy the source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Copy the built jar to the container
RUN cp target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
