@echo off
REM SMS Messaging Platform - Windows Development Setup Script
REM This script sets up the development environment and starts all services

echo 🚀 Starting SMS Messaging Platform Development Environment
echo ==========================================================

REM Check if Docker is available
docker --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Docker is not installed. Please install Docker Desktop first.
    exit /b 1
)

REM Check if Docker Compose is available
docker-compose --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Docker Compose is not available. Please ensure Docker Desktop is running.
    exit /b 1
)

REM Check if Java is available
java --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Java is not installed. Please install Java 17+ first.
    exit /b 1
)

REM Check if Maven is available
mvn --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Maven is not installed. Please install Maven first.
    exit /b 1
)

echo ✅ All prerequisites are met!

REM Create Kafka cluster ID if not exists
if not exist .cluster-id (
    echo 🔧 Generating Kafka cluster ID...
    docker run --rm confluentinc/cp-kafka:7.8.0 bash -c "kafka-storage.sh random-uuid" > .cluster-id
    echo ✅ Cluster ID generated
)

REM Start infrastructure
echo 🐳 Starting infrastructure services...
docker-compose up -d postgres kafka kafka-ui prometheus grafana

REM Wait for services to be ready
echo ⏳ Waiting for services to be ready...
timeout /t 15 /nobreak >nul

echo 🔍 Checking if services are ready...
timeout /t 5 /nobreak >nul

echo.
echo 🎉 Development environment is ready!
echo.
echo 📍 Available services:
echo    - PostgreSQL:     localhost:5432 (sms/sms/smsdb)
echo    - Kafka:          localhost:9092
echo    - Kafka UI:       http://localhost:8081
echo    - Prometheus:     http://localhost:9090
echo    - Grafana:        http://localhost:3000 (admin/admin)
echo.
echo 🚀 To start the services:
echo    1. SMS Service:      cd service-sms ^&^& mvnw.cmd quarkus:dev
echo    2. Processor Service: cd service-processor ^&^& mvnw.cmd quarkus:dev
echo.
echo 📡 Once running:
echo    - SMS Service API:   http://localhost:8080
echo    - Swagger UI:        http://localhost:8080/q/swagger-ui
echo    - Health Check:      http://localhost:8080/q/health
echo    - Metrics:           http://localhost:8080/q/metrics
echo    - Prometheus:        http://localhost:9090
echo    - Grafana Dashboard: http://localhost:3000
echo.
echo 🧪 Test the API:
echo    curl -X POST http://localhost:8080/v1/messages ^
echo      -H "Content-Type: application/json" ^
echo      -d "{\"sender\": \"+1234567890\", \"recipient\": \"+9876543210\", \"text\": \"Hello World!\"}"
echo.
