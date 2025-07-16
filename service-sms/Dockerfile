# Multi-stage build for Quarkus application
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /code
COPY pom.xml /code/pom.xml
COPY src /code/src
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

ENV LANGUAGE='en_US:en'

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the JAR file from build stage
COPY --from=build /code/target/quarkus-app/ /deployments/

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/q/health || exit 1

ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]
