# SMS Messaging Platform - Development Guide

## 🎯 Quick Start

### Prerequisites
- **Java 17+** - OpenJDK or Oracle JDK
- **Maven 3.8+** - Build tool
- **Docker & Docker Compose** - For infrastructure
- **Git** - Version control

### 1. Clone and Setup
```bash
git clone https://github.com/ChrysKoum/SMS-messaging-platform.git
cd intercomtelecom

# For Windows
scripts\dev-setup.bat

# For Linux/MacOS
chmod +x scripts/dev-setup.sh
./scripts/dev-setup.sh
```

### 2. Start Services

**Terminal 1 - SMS Service:**
```bash
cd service-sms
./mvnw quarkus:dev
# Windows: mvnw.cmd quarkus:dev
```

**Terminal 2 - Processor Service:**
```bash
cd service-processor
./mvnw quarkus:dev
# Windows: mvnw.cmd quarkus:dev
```

### 3. Test the API
```bash
# Send a message
curl -X POST http://localhost:8080/v1/messages \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "+1234567890",
    "recipient": "+9876543210", 
    "text": "Hello World!"
  }'

# Check message status
curl http://localhost:8080/v1/messages/{messageId}

# Get user messages
curl "http://localhost:8080/v1/users/+1234567890/messages?page=0&size=10"
```

## 🏗️ Architecture Overview

### System Flow
```
Client → SMS Service → Kafka → Processor Service → Callback → SMS Service
```

### Components

1. **SMS Service** (Port 8080)
   - REST API for message operations
   - PostgreSQL persistence
   - Kafka message publishing
   - Delivery report processing

2. **Processor Service** (Port 8082)
   - Kafka message consumption
   - SMS delivery simulation
   - HTTP callbacks to SMS Service

3. **Infrastructure**
   - PostgreSQL (Port 5432)
   - Kafka with KRaft (Port 9092)
   - Kafka UI (Port 8081)

## 📡 API Reference

### Send Message
```http
POST /v1/messages
Content-Type: application/json

{
  "sender": "+1234567890",
  "recipient": "+9876543210",
  "text": "Hello World!"
}
```

**Response:** `202 Accepted`
```json
{
  "id": "uuid",
  "sender": "+1234567890",
  "recipient": "+9876543210",
  "text": "Hello World!",
  "status": "PENDING",
  "created_at": "2024-01-01T12:00:00",
  "updated_at": "2024-01-01T12:00:00"
}
```

### Get Message
```http
GET /v1/messages/{messageId}
```

### Get User Messages
```http
GET /v1/users/{phoneNumber}/messages?page=0&size=20&status=SENT
```

### Internal Callback (Processor → SMS Service)
```http
POST /v1/internal/delivery-report
Content-Type: application/json

{
  "message_id": "uuid",
  "status": "SENT|FAILED",
  "failure_reason": "Optional error message"
}
```

## 🔧 Development

### Project Structure
```
intercomtelecom/
├── service-sms/                 # SMS Service
│   ├── src/main/java/com/intercom/sms/
│   │   ├── api/                 # REST controllers & DTOs
│   │   ├── domain/              # Entities & repositories
│   │   ├── service/             # Business logic
│   │   └── messaging/           # Kafka integration
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── db/migration/        # Flyway scripts
│   └── Dockerfile
├── service-processor/           # Processor Service
│   ├── src/main/java/com/intercom/processor/
│   │   ├── consumer/            # Kafka consumers
│   │   ├── service/             # Processing logic
│   │   └── client/              # HTTP clients
│   └── Dockerfile
├── docker-compose.yml           # Infrastructure
├── docs/                        # Documentation
└── scripts/                     # Utility scripts
```

### Key Technologies

- **Quarkus** - Supersonic, subatomic Java framework
- **Hibernate ORM with Panache** - Simplified data access
- **Kafka Reactive Messaging** - Async communication
- **PostgreSQL** - Persistent storage
- **Jackson** - JSON processing
- **Bean Validation** - Input validation
- **OpenAPI/Swagger** - API documentation

### Database Schema

**Messages Table:**
```sql
CREATE TABLE messages (
    id UUID PRIMARY KEY,
    sender VARCHAR(20) NOT NULL,
    recipient VARCHAR(20) NOT NULL,
    text VARCHAR(1600) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    failure_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Kafka Topics

- **sms.requests** - SMS processing requests
- **sms.retries.dead** - Failed message retries (DLQ)

## 🧪 Testing

### Unit Tests
```bash
cd service-sms
./mvnw test

cd service-processor
./mvnw test
```

### Integration Tests
```bash
./mvnw verify -Dquarkus.profile=test
```

### Manual Testing

**1. Start infrastructure:**
```bash
docker-compose up -d postgres kafka
```

**2. Test message flow:**
```bash
# Send message
MESSAGE_ID=$(curl -s -X POST http://localhost:8080/v1/messages \
  -H "Content-Type: application/json" \
  -d '{"sender": "+1234567890", "recipient": "+9876543210", "text": "Test"}' \
  | jq -r '.id')

# Check status (should be PENDING initially)
curl http://localhost:8080/v1/messages/$MESSAGE_ID

# Wait a few seconds for processing
sleep 5

# Check status again (should be SENT or FAILED)
curl http://localhost:8080/v1/messages/$MESSAGE_ID
```

## 📊 Monitoring

### Health Checks
- SMS Service: http://localhost:8080/q/health
- Processor Service: http://localhost:8082/q/health

### Metrics (Prometheus format)
- SMS Service: http://localhost:8080/q/metrics
- Processor Service: http://localhost:8082/q/metrics

### API Documentation
- Swagger UI: http://localhost:8080/q/swagger-ui
- OpenAPI Spec: http://localhost:8080/q/openapi

### Logs
```bash
# SMS Service logs
docker-compose logs -f sms-service

# Processor Service logs
docker-compose logs -f processor-service

# Kafka logs
docker-compose logs -f kafka
```

## 🐳 Docker Development

### Build Images
```bash
# Build all services
docker-compose build

# Build specific service
docker build -t sms-service ./service-sms
```

### Run with Docker
```bash
# Start everything
docker-compose up -d

# View logs
docker-compose logs -f

# Stop everything
docker-compose down
```

## 🔍 Troubleshooting

### Common Issues

**1. Port already in use:**
```bash
# Check what's using the port
netstat -tulpn | grep :8080
# Kill the process or use different ports
```

**2. Database connection failed:**
```bash
# Check PostgreSQL is running
docker-compose ps postgres
# Check connection
docker-compose exec postgres psql -U sms -d smsdb -c "SELECT 1"
```

**3. Kafka connection failed:**
```bash
# Check Kafka is running
docker-compose ps kafka
# Test Kafka connection
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

**4. Maven build failures:**
```bash
# Clean and rebuild
./mvnw clean install -DskipTests
# Update dependencies
./mvnw dependency:resolve
```

### Debug Mode

**Enable debug logging:**
```properties
# In application.properties
quarkus.log.category."com.intercom".level=DEBUG
```

**Remote debugging:**
```bash
# SMS Service
./mvnw quarkus:dev -Ddebug=5005

# Processor Service
./mvnw quarkus:dev -Ddebug=5006
```

## 🚀 Production Deployment

### Environment Variables
```bash
# Database
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://prod-db:5432/smsdb
QUARKUS_DATASOURCE_USERNAME=sms_user
QUARKUS_DATASOURCE_PASSWORD=secure_password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=prod-kafka:9092

# Processor callback
CALLBACK_URL=http://sms-service:8080/v1/internal/delivery-report
```

### Docker Compose Production
```bash
# Use production compose file
docker-compose -f docker-compose.prod.yml up -d
```

### Health Check Script
```bash
#!/bin/bash
# health-check.sh
curl -f http://localhost:8080/q/health || exit 1
curl -f http://localhost:8082/q/health || exit 1
```

## 📚 Additional Resources

- [Quarkus Documentation](https://quarkus.io/guides/)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [OpenAPI Specification](https://swagger.io/specification/)

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Make changes and add tests
4. Commit: `git commit -m 'Add amazing feature'`
5. Push: `git push origin feature/amazing-feature`
6. Submit pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.
