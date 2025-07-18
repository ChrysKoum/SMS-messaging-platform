# SMS Messaging Platform

A production-ready microservice-based SMS messaging platform built with **Java 17**, **Quarkus**, and **Kafka**.

## 🚀 Quick Start

```bash
# Clone and start the platform
git clone https://github.com/ChrysKoum/SMS-messaging-platform.git
cd SMS-messaging-platform
docker-compose up -d

# Test the SMS API
curl -X POST http://localhost:8080/v1/messages \
  -H "Content-Type: application/json" \
  -d '{"sender": "+1234567890", "recipient": "+0987654321", "text": "Hello World!"}'
```

**Access Points:**
- 📡 **API**: http://localhost:8080/q/swagger-ui
- 📊 **Monitoring**: http://localhost:3000 (Grafana)
- 💚 **Health**: http://localhost:8080/q/health

## 🏗️ Architecture

Two microservices with **async processing**:
- **SMS Service** → REST API + Kafka Producer
- **Processor Service** → Kafka Consumer + Delivery Simulation

**Tech Stack**: Java 17 • Quarkus 3.15.1 • Kafka • PostgreSQL • Docker

## � Documentation

| Topic | Documentation |
|-------|---------------|
| **🔧 Development Setup** | [`docs/development.md`](docs/development.md) |
| **🚀 Deployment Guide** | [`docs/deployment.md`](docs/deployment.md) |
| **📡 API Reference** | [`docs/api.md`](docs/api.md) |
| **🏛️ Architecture Decisions** | [`docs/decisions/`](docs/decisions/) |
| **📊 Project Status** | [`docs/PROJECT-STATUS.md`](docs/PROJECT-STATUS.md) |

## 🎯 Current Status

**🟢 FULLY OPERATIONAL** - All services healthy and production-ready.

See [`docs/PROJECT-STATUS.md`](docs/PROJECT-STATUS.md) for complete implementation details and testing results.
