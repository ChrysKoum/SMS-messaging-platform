#!/bin/bash

# SMS Messaging Platform - Development Setup Script
# This script sets up the development environment and starts all services

set -e

echo "🚀 Starting SMS Messaging Platform Development Environment"
echo "=========================================================="

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "🔍 Checking prerequisites..."

if ! command_exists docker; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command_exists docker-compose; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

if ! command_exists java; then
    echo "❌ Java is not installed. Please install Java 17+ first."
    exit 1
fi

if ! command_exists mvn; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

echo "✅ All prerequisites are met!"

# Create Kafka cluster ID if not exists
if [ ! -f .cluster-id ]; then
    echo "🔧 Generating Kafka cluster ID..."
    docker run --rm confluentinc/cp-kafka:7.8.0 \
        bash -c "kafka-storage.sh random-uuid" > .cluster-id
    echo "✅ Cluster ID generated: $(cat .cluster-id)"
fi

# Start infrastructure
echo "🐳 Starting infrastructure services..."
docker-compose up -d postgres kafka kafka-ui prometheus grafana

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 15

# Check if PostgreSQL is ready
echo "🔍 Checking PostgreSQL connection..."
until docker-compose exec -T postgres pg_isready -U sms; do
    echo "⏳ Waiting for PostgreSQL..."
    sleep 2
done
echo "✅ PostgreSQL is ready!"

# Check if Kafka is ready
echo "🔍 Checking Kafka connection..."
until docker-compose exec -T kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >/dev/null 2>&1; do
    echo "⏳ Waiting for Kafka..."
    sleep 2
done
echo "✅ Kafka is ready!"

# Build services
echo "🔨 Building SMS Service..."
cd service-sms
./mvnw clean compile -q
cd ..

echo "🔨 Building Processor Service..."
cd service-processor
./mvnw clean compile -q || echo "⚠️  Processor service build failed - will fix during development"
cd ..

echo ""
echo "🎉 Development environment is ready!"
echo ""
echo "📍 Available services:"
echo "   - PostgreSQL:     localhost:5432 (sms/sms/smsdb)"
echo "   - Kafka:          localhost:9092"
echo "   - Kafka UI:       http://localhost:8081"
echo "   - Prometheus:     http://localhost:9090"
echo "   - Grafana:        http://localhost:3000 (admin/admin)"
echo ""
echo "🚀 To start the services:"
echo "   1. SMS Service:      cd service-sms && ./mvnw quarkus:dev"
echo "   2. Processor Service: cd service-processor && ./mvnw quarkus:dev"
echo ""
echo "📡 Once running:"
echo "   - SMS Service API:   http://localhost:8080"
echo "   - Swagger UI:        http://localhost:8080/q/swagger-ui"
echo "   - Health Check:      http://localhost:8080/q/health"
echo "   - Metrics:           http://localhost:8080/q/metrics"
echo "   - Prometheus:        http://localhost:9090"
echo "   - Grafana Dashboard: http://localhost:3000"
echo ""
echo "🧪 Test the API:"
echo '   curl -X POST http://localhost:8080/v1/messages \'
echo '     -H "Content-Type: application/json" \'
echo '     -d "{"sender": "+1234567890", "recipient": "+9876543210", "text": "Hello World!"}"'
echo ""
