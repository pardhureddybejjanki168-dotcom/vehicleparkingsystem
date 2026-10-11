# Multi-stage Docker build for Spring Boot Backend
# Uses offline build to avoid network timeouts on slow CI/CD networks

FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy Maven settings with extended timeouts + retry
COPY maven-settings.xml /root/.m2/settings.xml

# Step 1: Download ALL dependencies (runtime + plugins) and cache them
COPY pom.xml .
RUN mvn -B \
    dependency:go-offline \
    dependency:resolve \
    dependency:resolve-plugins \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=180 \
    -Dmaven.wagon.http.retryHandler.count=10

# Step 2: Build using OFFLINE mode (-o) — zero network calls, uses cached deps only
COPY src ./src
RUN mvn clean package -DskipTests -o

# Runtime stage — minimal JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
