# ==============================
# 1️⃣ Build Stage
# ==============================
FROM openjdk:21-jdk-slim AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first for caching dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Ensure the Maven wrapper is executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application JAR (skip tests for CI/CD speed)
RUN ./mvnw clean package -DskipTests -B


# ==============================
# 2️⃣ Runtime Stage
# ==============================
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy built jar from previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
