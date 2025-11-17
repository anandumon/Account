# ============================================
# 1️⃣ Build Stage (Maven + JDK)
# ============================================
FROM eclipse-temurin:21-jdk AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper + pom.xml for caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Make mvnw executable
RUN chmod +x mvnw

# Pre-download dependencies (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copy the entire project
COPY src ./src

# Build the application (skip tests)
RUN ./mvnw clean package -DskipTests -B


# ============================================
# 2️⃣ Runtime Stage (Smaller JDK)
# ============================================
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy only the final JAR from builder
COPY --from=build /app/target/*.jar app.jar

# Expose Port
EXPOSE 8080

# Run the application with optimized JVM flags
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=80.0", "-jar", "app.jar"]
