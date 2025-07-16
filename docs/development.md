# Development Setup Guide

## Overview

This guide covers setting up a local development environment for the SMS Messaging Platform, including IDE configuration, debugging, and development workflows.

## Prerequisites

### Required Software

- **Java Development Kit (JDK)**: 17 or later
  ```bash
  # Verify Java version
  java -version
  javac -version
  ```

- **Apache Maven**: 3.8 or later
  ```bash
  # Verify Maven version
  mvn -version
  ```

- **Docker & Docker Compose**: Latest stable version
  ```bash
  # Verify Docker
  docker --version
  docker-compose --version
  ```

- **Git**: For version control
  ```bash
  git --version
  ```

### Recommended Tools

- **IDE**: IntelliJ IDEA, Eclipse, or VS Code
- **HTTP Client**: Postman, Insomnia, or curl
- **Database Client**: pgAdmin, DBeaver, or DataGrip
- **Kafka Tool**: Kafka UI (included in Docker Compose)

## Project Setup

### 1. Clone and Initialize

```bash
# Clone the repository
git clone <repository-url>
cd intercomtelecom

# Verify project structure
ls -la
```

**Expected Structure**:
```
intercomtelecom/
├── docker-compose.yml
├── service-sms/
│   ├── pom.xml
│   ├── src/
│   └── Dockerfile
├── service-processor/
│   ├── pom.xml
│   ├── src/
│   └── Dockerfile
├── docs/
├── scripts/
└── README.md
```

### 2. Environment Setup

**Option A: Full Docker Development (Recommended)**
```bash
# Start entire platform
docker-compose up -d

# Verify services
docker-compose ps
```

**Option B: Hybrid Development**
```bash
# Start infrastructure only
docker-compose up -d postgres kafka kafka-ui prometheus grafana

# Services will be run locally via IDE or Maven
```

## IDE Configuration

### IntelliJ IDEA Setup

#### 1. Project Import
1. Open IntelliJ IDEA
2. Choose "Open or Import"
3. Select the `intercomtelecom` directory
4. Choose "Maven" when prompted
5. Wait for Maven to download dependencies

#### 2. Quarkus Plugin
1. Go to `File → Settings → Plugins`
2. Search for "Quarkus"
3. Install the official Quarkus plugin
4. Restart IDE

#### 3. Run Configurations

**SMS Service Configuration**:
- **Name**: SMS Service Dev
- **Main Class**: `io.quarkus.runner.GeneratedMain`
- **Module**: `service-sms`
- **Working Directory**: `$MODULE_WORKING_DIR$`
- **Environment Variables**:
  ```
  QUARKUS_PROFILE=dev
  QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/smsdb
  KAFKA_BOOTSTRAP_SERVERS=localhost:9092
  ```

**Processor Service Configuration**:
- **Name**: Processor Service Dev
- **Main Class**: `io.quarkus.runner.GeneratedMain`
- **Module**: `service-processor`
- **Working Directory**: `$MODULE_WORKING_DIR$`
- **Environment Variables**:
  ```
  QUARKUS_PROFILE=dev
  KAFKA_BOOTSTRAP_SERVERS=localhost:9092
  SMS_SERVICE_URL=http://localhost:8080
  QUARKUS_HTTP_PORT=8082
  ```

#### 4. Debug Configuration

**Enable Remote Debug**:
```bash
# Run with debug enabled
cd service-sms
./mvnw quarkus:dev -Ddebug=5005
```

In IntelliJ:
1. `Run → Edit Configurations`
2. Add "Remote JVM Debug"
3. Set port to `5005`
4. Connect debugger

### VS Code Setup

#### 1. Extensions
Install the following extensions:
- **Extension Pack for Java** (Microsoft)
- **Quarkus** (Red Hat)
- **Docker** (Microsoft)
- **REST Client** (Huachao Mao)

#### 2. Workspace Configuration

Create `.vscode/settings.json`:
```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.server.launchMode": "Standard",
  "quarkus.tools.debug.port": 5005,
  "quarkus.tools.starter.showExtensionDescriptions": true
}
```

#### 3. Launch Configuration

Create `.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "SMS Service Debug",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005,
      "projectName": "service-sms"
    },
    {
      "type": "java", 
      "name": "Processor Service Debug",
      "request": "attach",
      "hostName": "localhost",
      "port": 5006,
      "projectName": "service-processor"
    }
  ]
}
```

### Eclipse Setup

#### 1. Import Projects
1. `File → Import → Existing Maven Projects`
2. Browse to `intercomtelecom` directory
3. Select both `service-sms` and `service-processor`
4. Import

#### 2. Install Quarkus Tools
1. `Help → Eclipse Marketplace`
2. Search for "Quarkus Tools"
3. Install and restart

## Development Workflows

### 1. Local Development

**Start Infrastructure**:
```bash
# Terminal 1 - Infrastructure
docker-compose up -d postgres kafka kafka-ui prometheus grafana
```

**Run SMS Service**:
```bash
# Terminal 2 - SMS Service
cd service-sms
./mvnw quarkus:dev
```

**Run Processor Service**:
```bash
# Terminal 3 - Processor Service
cd service-processor
./mvnw quarkus:dev
```

### 2. Hot Reload Development

Quarkus provides hot reload for fast development:

```bash
cd service-sms
./mvnw quarkus:dev
```

**Features**:
- **Automatic reload** on Java file changes
- **Live reload** for static resources
- **Dev UI** available at http://localhost:8080/q/dev/
- **Continuous testing** with `r` key

### 3. Testing Workflow

**Unit Tests**:
```bash
# Run all tests
./mvnw test

# Run specific test
./mvnw test -Dtest=MessageServiceTest

# Run with coverage
./mvnw test jacoco:report
```

**Integration Tests**:
```bash
# Run integration tests
./mvnw verify -Dquarkus.profile=test

# Run with Testcontainers
./mvnw verify -Dquarkus.test.profile=test-containers
```

**API Testing**:
```bash
# Using curl
curl -X POST http://localhost:8080/v1/messages \
  -H "Content-Type: application/json" \
  -d '{"sender":"+1234567890","recipient":"+1987654321","text":"Test"}'

# Using HTTPie
http POST localhost:8080/v1/messages \
  sender="+1234567890" \
  recipient="+1987654321" \
  text="Test message"
```

## Database Development

### 1. Database Access

**Connection Details**:
- **Host**: localhost
- **Port**: 5432
- **Database**: smsdb
- **Username**: smsuser
- **Password**: smspass

**Connection via psql**:
```bash
# Connect to database
docker exec -it sms-postgres psql -U smsuser -d smsdb

# Common queries
\dt                          -- List tables
SELECT * FROM messages;      -- View messages
SELECT * FROM flyway_schema_history;  -- View migrations
```

### 2. Database Migrations

**Flyway Migrations** are located in `service-sms/src/main/resources/db/migration/`:

```sql
-- V1__Initial_schema.sql
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender VARCHAR(20) NOT NULL,
    recipient VARCHAR(20) NOT NULL,
    text TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_sender ON messages(sender);
CREATE INDEX idx_messages_recipient ON messages(recipient);
```

**Add New Migration**:
1. Create file: `V2__Add_new_feature.sql`
2. Write SQL changes
3. Restart application (Flyway runs automatically)

### 3. Database Testing

**Test Data Setup**:
```sql
-- Insert test messages
INSERT INTO messages (sender, recipient, text, status) VALUES 
  ('+1234567890', '+1987654321', 'Test message 1', 'PENDING'),
  ('+1555000000', '+1444000000', 'Test message 2', 'SENT'),
  ('+1777000000', '+1888000000', 'Test message 3', 'FAILED');
```

## Kafka Development

### 1. Kafka UI Access

**Kafka UI**: http://localhost:8081

**Features**:
- Browse topics and partitions
- View message content
- Monitor consumer groups
- Topic management

### 2. Topic Management

**List Topics**:
```bash
docker exec -it sms-kafka kafka-topics \
  --bootstrap-server localhost:9092 --list
```

**Create Topic**:
```bash
docker exec -it sms-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create --topic sms.requests \
  --partitions 3 --replication-factor 1
```

**View Messages**:
```bash
docker exec -it sms-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic sms.requests --from-beginning
```

### 3. Message Testing

**Produce Test Message**:
```bash
docker exec -it sms-kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic sms.requests

# Then type JSON message:
{"id":"test-123","sender":"+1234567890","recipient":"+1987654321","text":"Test"}
```

## Code Quality and Standards

### 1. Code Formatting

**Maven Formatter Plugin** (configured in pom.xml):
```bash
# Format code
./mvnw formatter:format

# Validate formatting
./mvnw formatter:validate
```

**IDE Formatting**:
- Import code style from `ide-config/` directory
- Enable format on save

### 2. Static Analysis

**SpotBugs**:
```bash
./mvnw spotbugs:check
```

**Checkstyle**:
```bash
./mvnw checkstyle:check
```

**PMD**:
```bash
./mvnw pmd:check
```

### 3. Dependency Management

**Check for Updates**:
```bash
./mvnw versions:display-dependency-updates
./mvnw versions:display-plugin-updates
```

**Security Audit**:
```bash
./mvnw org.owasp:dependency-check-maven:check
```

## Debugging and Troubleshooting

### 1. Application Debugging

**Enable Debug Logging**:
```properties
# application.properties
quarkus.log.level=DEBUG
quarkus.log.category."com.intercom".level=DEBUG
```

**Remote Debugging**:
```bash
# Start with debug port
./mvnw quarkus:dev -Ddebug=5005

# Or with custom port
./mvnw quarkus:dev -Ddebug=5006
```

### 2. Common Issues

**Port Already in Use**:
```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use different port
./mvnw quarkus:dev -Dquarkus.http.port=8081
```

**Database Connection Issues**:
```bash
# Check if PostgreSQL is running
docker-compose ps postgres

# View PostgreSQL logs
docker-compose logs postgres

# Test connection
docker exec -it sms-postgres pg_isready -U smsuser
```

**Kafka Connection Issues**:
```bash
# Check Kafka status
docker-compose ps kafka

# View Kafka logs
docker-compose logs kafka

# Test connectivity
docker exec -it sms-kafka kafka-broker-api-versions \
  --bootstrap-server localhost:9092
```

### 3. Performance Profiling

**JVM Profiling**:
```bash
# Start with JFR profiling
./mvnw quarkus:dev -Djvm.args="-XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=profile.jfr"
```

**Memory Analysis**:
```bash
# Heap dump
jcmd <PID> GC.run_finalization
jcmd <PID> VM.gc
jcmd <PID> GC.dump /tmp/heapdump.hprof
```

## Configuration Management

### 1. Environment-Specific Properties

**application.properties** (default):
```properties
# Database
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/smsdb

# Kafka
kafka.bootstrap.servers=localhost:9092

# Logging
quarkus.log.level=INFO
```

**application-dev.properties** (development):
```properties
# Development overrides
quarkus.log.level=DEBUG
quarkus.hibernate-orm.log.sql=true
quarkus.dev-ui.always-include=true
```

**application-test.properties** (testing):
```properties
# Test overrides
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb
```

### 2. Configuration Profiles

**Activate Profile**:
```bash
# Via Maven
./mvnw quarkus:dev -Dquarkus.profile=dev

# Via Environment Variable
export QUARKUS_PROFILE=dev
./mvnw quarkus:dev

# Via System Property
java -Dquarkus.profile=dev -jar app.jar
```

## Continuous Integration

### 1. Pre-commit Hooks

Create `.git/hooks/pre-commit`:
```bash
#!/bin/bash
echo "Running pre-commit checks..."

# Run tests
./mvnw test
if [ $? -ne 0 ]; then
  echo "Tests failed. Commit aborted."
  exit 1
fi

# Check formatting
./mvnw formatter:validate
if [ $? -ne 0 ]; then
  echo "Code formatting issues. Run 'mvn formatter:format' and try again."
  exit 1
fi

echo "Pre-commit checks passed."
```

### 2. GitHub Actions Example

`.github/workflows/ci.yml`:
```yaml
name: CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        
    - name: Run tests
      run: ./mvnw verify
```

## Development Best Practices

### 1. Code Organization

- **Package Structure**: Follow domain-driven design
- **Naming Conventions**: Use clear, descriptive names
- **Method Size**: Keep methods small and focused
- **Class Responsibilities**: Single Responsibility Principle

### 2. Testing Practices

- **Unit Tests**: Test business logic in isolation
- **Integration Tests**: Test component interactions
- **API Tests**: Test external interfaces
- **Test Coverage**: Aim for 80%+ coverage

### 3. Git Workflow

```bash
# Feature development
git checkout -b feature/sms-retry-mechanism
git add .
git commit -m "feat: add SMS retry mechanism"
git push origin feature/sms-retry-mechanism

# Create pull request for review
```

**Commit Message Format**:
```
feat: add new feature
fix: bug fix
docs: documentation update
test: add tests
refactor: code refactoring
```

For deployment instructions, see the [Deployment Guide](deployment.md).
