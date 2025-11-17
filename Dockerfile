# ============================
# Stage 1 — Build the project
# ============================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests clean package

# ============================
# Stage 2 — Create final image
# ============================
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/account-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Production Run Command
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]

