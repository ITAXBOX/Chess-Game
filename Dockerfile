# Use an official OpenJDK 22 runtime as a parent image
FROM openjdk:22-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper script and pom.xml
COPY mvnw pom.xml ./

# Copy the Maven wrapper files directory
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x mvnw

# Copy the source code
COPY src ./src

# Build the application (skip tests)
RUN ./mvnw clean package -DskipTests

# Copy the built jar to the container root
RUN cp target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
