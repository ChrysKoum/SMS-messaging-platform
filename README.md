# SMS Messaging Platform

A microservice-based SMS messaging platform built with Java, Quarkus, and Kafka.

## 🏗️ Architecture Overview

The system consists of two main microservices:

1. **SMS Service**: 
   - Exposes REST endpoints to send messages and list user messages
   - Performs synchronous validations
   - Processes accepted messages asynchronously via Kafka

2. **Processor Service**:
   - Listens for incoming SMS requests from Kafka
   - Simulates message processing
   - Notifies the SMS Service upon completion (success/failure)

## 🛠️ Technology Stack

- **Java 17+**
- **Quarkus** - Supersonic, Subatomic Java Framework
- **Kafka (KRaft mode)** - Message broker for asynchronous processing
- **PostgreSQL** - Message persistence
- **Maven** - Build tool
- **Docker & Docker Compose** - Containerization

## 🚀 Quick Start

### Prerequisites
- JDK 17+
- Docker & Docker Compose
- Maven 3.8+

### Running the Application

1. **Clone the repository**
   ```bash
   git clone https://github.com/ChrysKoum/SMS-messaging-platform.git
   cd intercomtelecom
   ```

2. **Start the infrastructure**
   ```bash
   docker-compose up -d postgres kafka prometheus grafana
   ```

3. **Run the services**
   ```bash
   # Terminal 1 - SMS Service
   cd service-sms
   ./mvnw quarkus:dev

   # Terminal 2 - Processor Service  
   cd service-processor
   ./mvnw quarkus:dev
   ```

4. **Access the application**
   - SMS Service API: http://localhost:8080
   - Swagger UI: http://localhost:8080/q/swagger-ui
   - Health Check: http://localhost:8080/q/health
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3000 (admin/admin)

## 📡 API Endpoints

### SMS Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/v1/messages` | Send SMS message |
| GET | `/v1/messages/{id}` | Get message by ID |
| GET | `/v1/users/{userId}/messages` | List user messages |
| POST | `/v1/internal/delivery-report` | Callback for delivery status |

### Example Usage

**Send SMS:**
```bash
curl -X POST http://localhost:8080/v1/messages \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "+1234567890",
    "recipient": "+0987654321", 
    "text": "Hello World!"
  }'
```

**Get Message:**
```bash
curl http://localhost:8080/v1/messages/{messageId}
```

## 🔧 Development

### Project Structure
```
intercomtelecom/
├── docker-compose.yml           # Infrastructure setup
├── service-sms/                 # SMS Service
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── src/test/java/
│   └── pom.xml
├── service-processor/           # Processor Service  
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── src/test/java/
│   └── pom.xml
├── docs/                        # Documentation
└── scripts/                     # Utility scripts
```

### Running Tests
```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw verify

# Integration tests with Testcontainers
./mvnw verify -Dquarkus.profile=test
```

### Building Docker Images
```bash
# Build all services
docker-compose build

# Build specific service
docker build -t sms-service ./service-sms
```

## 📊 Monitoring & Observability

### Built-in Monitoring Stack
- **Prometheus**: http://localhost:9090 - Metrics collection
- **Grafana**: http://localhost:3000 - Dashboards (admin/admin)
- **Health Checks**: `/q/health` - Service health status
- **Metrics**: `/q/metrics` - Prometheus format metrics
- **OpenAPI**: `/q/openapi` - API specification

### Business Metrics
- `sms_sent_total` - Successfully sent messages
- `sms_failed_total` - Failed message deliveries  
- `sms_send_duration` - Send request processing time
- `sms_callback_duration` - Delivery callback processing time

### System Metrics
- HTTP request rates and latency
- JVM memory and garbage collection
- Kafka producer/consumer metrics
- Database connection pool stats

See [Monitoring Guide](docs/monitoring.md) for detailed setup and usage.
- **Swagger UI**: `/q/swagger-ui`

## 🔐 Security Features

- **Authentication**: Token-based authentication for API endpoints
- **Input Validation**: Comprehensive validation with error handling
- **Rate Limiting**: Built-in rate limiting for API protection

## 🧪 Testing Strategy

- **Unit Tests**: JUnit 5 + Mockito
- **Integration Tests**: RestAssured + Testcontainers
- **Contract Tests**: OpenAPI validation
- **Performance Tests**: Load testing scenarios

## 📈 Performance & Scalability

- **Asynchronous Processing**: Non-blocking message processing
- **Database Connection Pooling**: Optimized database connections
- **Kafka Partitioning**: Scalable message distribution
- **Caching**: Redis integration for frequently accessed data

## 🚀 Deployment

### Docker Compose (Development)
```bash
docker-compose up -d
```

### Kubernetes (Production)
```bash
kubectl apply -f k8s/
```

## 📝 Documentation

- [API Documentation](docs/api.md)
- [Architecture Decision Records](docs/decisions/)
- [Deployment Guide](docs/deployment.md)
- [Development Setup](docs/development.md)
