# Deployment Guide

## Overview

This guide covers deploying the SMS Messaging Platform in various environments, from local development to production deployment.

## Prerequisites

### System Requirements
- **Docker**: 20.10+ with Docker Compose
- **Java**: 17+ (for development builds)
- **Maven**: 3.8+ (for development builds)
- **Memory**: Minimum 4GB RAM recommended
- **Disk Space**: 2GB available space

### Network Requirements
- **Port 8080**: SMS Service API
- **Port 8082**: Processor Service API
- **Port 5432**: PostgreSQL database
- **Port 9092-9093**: Kafka brokers
- **Port 8081**: Kafka UI
- **Port 3000**: Grafana dashboards
- **Port 9090**: Prometheus metrics

## Quick Deployment (Recommended)

### 1. Single Command Deployment

```bash
# Clone repository
git clone https://github.com/ChrysKoum/SMS-messaging-platform.git
cd SMS-messaging-platform

# Start entire platform
docker-compose up -d

# Verify deployment
docker-compose ps
```

**Expected Output**:
```
NAME                IMAGE                               STATUS
processor-service   intercomtelecom-processor-service   Up (healthy)
sms-grafana         grafana/grafana:latest              Up  
sms-kafka           confluentinc/cp-kafka:7.8.0         Up (healthy)
sms-kafka-ui        provectuslabs/kafka-ui:latest       Up
sms-postgres        postgres:16-alpine                  Up (healthy)
sms-prometheus      prom/prometheus:latest              Up
sms-service         intercomtelecom-sms-service         Up (healthy)
```

### 2. Verify Deployment

```bash
# Check service health
curl http://localhost:8080/q/health

# Test API
curl -X POST http://localhost:8080/v1/messages \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "+1234567890",
    "recipient": "+1987654321",
    "text": "Deployment test message"
  }'

# Access web interfaces
# - Swagger UI: http://localhost:8080/q/swagger-ui
# - Kafka UI: http://localhost:8081
# - Grafana: http://localhost:3000 (admin/admin)
```

## Environment-Specific Deployments

### Development Environment

**Option 1: Full Docker (Recommended for consistency)**
```bash
docker-compose up -d
```

**Option 2: Hybrid (Infrastructure + Local Development)**
```bash
# Start infrastructure only
docker-compose up -d postgres kafka kafka-ui prometheus grafana

# Run services locally
cd service-sms
./mvnw quarkus:dev

# In another terminal
cd service-processor  
./mvnw quarkus:dev
```

### Staging/Production Environment

#### Production Docker Compose

Create `docker-compose.prod.yml`:
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: smsdb
      POSTGRES_USER: smsuser
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped
    
  kafka:
    image: confluentinc/cp-kafka:7.8.0
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_LISTENERS: CONTROLLER://0.0.0.0:29093,PLAINTEXT://0.0.0.0:9092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://${KAFKA_HOST}:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:29093
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: true
    volumes:
      - kafka_data:/var/lib/kafka/data
    restart: unless-stopped

  sms-service:
    image: intercomtelecom-sms-service:${IMAGE_TAG:-latest}
    environment:
      QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://postgres:5432/smsdb
      QUARKUS_DATASOURCE_USERNAME: smsuser
      QUARKUS_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      QUARKUS_LOG_LEVEL: INFO
    depends_on:
      - postgres
      - kafka
    restart: unless-stopped
    
  processor-service:
    image: intercomtelecom-processor-service:${IMAGE_TAG:-latest}
    environment:
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      SMS_SERVICE_URL: http://sms-service:8080
      QUARKUS_LOG_LEVEL: INFO
    depends_on:
      - kafka
      - sms-service
    restart: unless-stopped

volumes:
  postgres_data:
  kafka_data:
```

**Deploy to production**:
```bash
# Set environment variables
export DB_PASSWORD=secure_password_here
export KAFKA_HOST=your-kafka-host.com
export IMAGE_TAG=v1.0.0

# Deploy
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes Deployment

#### Namespace and ConfigMap

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: sms-platform
---
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: sms-config
  namespace: sms-platform
data:
  kafka-bootstrap-servers: "kafka-service:9092"
  postgres-host: "postgres-service"
  postgres-database: "smsdb"
```

#### Database Deployment

```yaml
# k8s/postgres.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: sms-platform
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:16-alpine
        env:
        - name: POSTGRES_DB
          value: "smsdb"
        - name: POSTGRES_USER
          value: "smsuser"
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: password
        ports:
        - containerPort: 5432
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
      volumes:
      - name: postgres-storage
        persistentVolumeClaim:
          claimName: postgres-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: postgres-service
  namespace: sms-platform
spec:
  selector:
    app: postgres
  ports:
  - port: 5432
    targetPort: 5432
```

#### SMS Service Deployment

```yaml
# k8s/sms-service.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sms-service
  namespace: sms-platform
spec:
  replicas: 2
  selector:
    matchLabels:
      app: sms-service
  template:
    metadata:
      labels:
        app: sms-service
    spec:
      containers:
      - name: sms-service
        image: intercomtelecom-sms-service:latest
        env:
        - name: QUARKUS_DATASOURCE_JDBC_URL
          value: "jdbc:postgresql://postgres-service:5432/smsdb"
        - name: KAFKA_BOOTSTRAP_SERVERS
          valueFrom:
            configMapKeyRef:
              name: sms-config
              key: kafka-bootstrap-servers
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /q/health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /q/health/ready
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: sms-service
  namespace: sms-platform
spec:
  selector:
    app: sms-service
  ports:
  - port: 8080
    targetPort: 8080
  type: LoadBalancer
```

**Deploy to Kubernetes**:
```bash
kubectl apply -f k8s/
```

## Configuration Management

### Environment Variables

#### SMS Service
| Variable | Description | Default |
|----------|-------------|---------|
| `QUARKUS_DATASOURCE_JDBC_URL` | PostgreSQL connection URL | `jdbc:postgresql://postgres:5432/smsdb` |
| `QUARKUS_DATASOURCE_USERNAME` | Database username | `smsuser` |
| `QUARKUS_DATASOURCE_PASSWORD` | Database password | `smspass` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | `kafka:9092` |
| `QUARKUS_LOG_LEVEL` | Application log level | `INFO` |

#### Processor Service
| Variable | Description | Default |
|----------|-------------|---------|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | `kafka:9092` |
| `SMS_SERVICE_URL` | SMS service callback URL | `http://sms-service:8080` |
| `QUARKUS_LOG_LEVEL` | Application log level | `INFO` |

### Secrets Management

**For Docker Compose**:
```bash
# Create .env file
cat > .env << EOF
DB_PASSWORD=secure_password_here
KAFKA_PASSWORD=kafka_password_here
GRAFANA_ADMIN_PASSWORD=admin_password_here
EOF
```

**For Kubernetes**:
```bash
# Create secrets
kubectl create secret generic postgres-secret \
  --from-literal=password=secure_password_here \
  -n sms-platform

kubectl create secret generic kafka-secret \
  --from-literal=password=kafka_password_here \
  -n sms-platform
```

## Monitoring and Logging

### Health Checks

All services provide health check endpoints:

```bash
# SMS Service
curl http://localhost:8080/q/health

# Processor Service  
curl http://localhost:8082/q/health

# Expected response
{
  "status": "UP",
  "checks": [
    {
      "name": "Database connection health check",
      "status": "UP"
    },
    {
      "name": "Kafka health check", 
      "status": "UP"
    }
  ]
}
```

### Log Aggregation

**Docker Compose with ELK Stack**:
```yaml
# Add to docker-compose.yml
  elasticsearch:
    image: elasticsearch:8.8.0
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    
  logstash:
    image: logstash:8.8.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
      
  kibana:
    image: kibana:8.8.0
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
```

### Metrics Collection

Prometheus configuration is included in the Docker Compose setup:

```yaml
# prometheus.yml (already configured)
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'sms-service'
    static_configs:
      - targets: ['sms-service:8080']
    metrics_path: '/q/metrics'
    
  - job_name: 'processor-service'  
    static_configs:
      - targets: ['processor-service:8081']
    metrics_path: '/q/metrics'
```

## Security Considerations

### Production Security Checklist

- [ ] **Change default passwords** for all services
- [ ] **Enable HTTPS/TLS** for all external endpoints
- [ ] **Configure firewall rules** to restrict access
- [ ] **Implement API authentication** (JWT/API keys)
- [ ] **Enable audit logging** for all API calls
- [ ] **Set up intrusion detection**
- [ ] **Configure backup and disaster recovery**
- [ ] **Implement rate limiting**
- [ ] **Use secrets management** (Vault, Kubernetes secrets)
- [ ] **Regular security updates** for base images

### Network Security

```yaml
# Docker Compose with network isolation
networks:
  frontend:
    driver: bridge
  backend:
    driver: bridge
    internal: true

services:
  sms-service:
    networks:
      - frontend
      - backend
      
  postgres:
    networks:
      - backend  # Only accessible from backend
```

## Backup and Recovery

### Database Backup

```bash
# Automated backup script
#!/bin/bash
BACKUP_DIR="/backups"
DB_CONTAINER="sms-postgres"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Create backup
docker exec $DB_CONTAINER pg_dump -U smsuser smsdb > \
  $BACKUP_DIR/smsdb_backup_$TIMESTAMP.sql

# Compress
gzip $BACKUP_DIR/smsdb_backup_$TIMESTAMP.sql

# Clean old backups (keep 7 days)
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete
```

### Disaster Recovery

```bash
# Restore from backup
docker exec -i sms-postgres psql -U smsuser smsdb < backup.sql

# Or restore in new environment
docker-compose down
docker volume rm intercomtelecom_postgres_data
docker-compose up -d postgres
# Wait for postgres to initialize
docker exec -i sms-postgres psql -U smsuser smsdb < backup.sql
docker-compose up -d
```

## Scaling Considerations

### Horizontal Scaling

**SMS Service Scaling**:
```yaml
# Scale SMS service
services:
  sms-service:
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
```

**Kafka Scaling**:
```yaml
# Add Kafka brokers
services:
  kafka-1:
    # ... kafka config with KAFKA_NODE_ID: 1
  kafka-2:
    # ... kafka config with KAFKA_NODE_ID: 2  
  kafka-3:
    # ... kafka config with KAFKA_NODE_ID: 3
```

### Database Scaling

**Read Replicas**:
```yaml
services:
  postgres-primary:
    # ... primary configuration
    
  postgres-replica:
    image: postgres:16-alpine
    environment:
      PGUSER: replicator
      POSTGRES_PASSWORD: replica_password
      POSTGRES_PRIMARY_USER: smsuser
      POSTGRES_PRIMARY_PASSWORD: smspass
      POSTGRES_PRIMARY_DB: smsdb
      POSTGRES_PRIMARY_PORT: 5432
```

## Troubleshooting

### Common Issues

**Services not starting**:
```bash
# Check logs
docker-compose logs sms-service
docker-compose logs processor-service

# Check system resources
docker system df
docker system prune  # Clean up if needed
```

**Database connection issues**:
```bash
# Test database connectivity
docker exec -it sms-postgres psql -U smsuser -d smsdb -c "SELECT 1;"

# Check database logs
docker-compose logs postgres
```

**Kafka connectivity issues**:
```bash
# Check Kafka status
docker exec -it sms-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Check Kafka logs
docker-compose logs kafka
```

**Health check failures**:
```bash
# Detailed health check
curl http://localhost:8080/q/health | jq '.'

# Check individual components
curl http://localhost:8080/q/health/live
curl http://localhost:8080/q/health/ready
```

### Performance Tuning

**JVM Tuning**:
```yaml
services:
  sms-service:
    environment:
      JAVA_OPTS: "-Xms256m -Xmx512m -XX:+UseG1GC"
```

**Database Tuning**:
```yaml
services:
  postgres:
    command: >
      postgres
      -c max_connections=200
      -c shared_buffers=256MB
      -c work_mem=4MB
```

For additional support, see the [Development Setup](development.md) guide.
