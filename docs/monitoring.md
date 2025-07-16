# 📊 SMS Platform - Monitoring & Observability Guide

## 🎯 Overview

The SMS messaging platform includes a comprehensive observability stack with Prometheus metrics collection and Grafana dashboards for monitoring application and business metrics.

## 🏗️ Monitoring Architecture

```
Applications → Micrometer → Prometheus → Grafana
     ↓
[Business Metrics]  [System Metrics]  [Dashboards]
```

### Components
- **Prometheus** - Metrics collection and storage
- **Grafana** - Visualization and dashboards  
- **Micrometer** - Application metrics instrumentation

## 🚀 Quick Start

### 1. Start Monitoring Stack
```bash
# Start all services including monitoring
docker-compose up -d

# Or start just monitoring services
docker-compose up -d prometheus grafana
```

### 2. Access Dashboards
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Application Metrics**: http://localhost:8080/q/metrics

## 📈 Available Metrics

### Business Metrics

| Metric | Type | Description |
|--------|------|-------------|
| `sms_send_attempts_total` | Counter | Total SMS send attempts |
| `sms_sent_total` | Counter | Successfully sent messages |
| `sms_failed_total` | Counter | Failed message deliveries |
| `sms_queued_total` | Counter | Messages queued for processing |
| `sms_queue_failed_total` | Counter | Failed to queue messages |
| `sms_send_duration` | Timer | Time to process send requests |
| `sms_callback_duration` | Timer | Time to process delivery callbacks |

### System Metrics (Auto-Generated)

| Metric | Description |
|--------|-------------|
| `http_server_requests_seconds` | HTTP request latency |
| `http_server_requests_seconds_count` | HTTP request count |
| `jvm_memory_used_bytes` | JVM memory usage |
| `jvm_gc_pause_seconds` | Garbage collection times |
| `kafka_producer_*` | Kafka producer metrics |
| `kafka_consumer_*` | Kafka consumer metrics |

## 📊 Grafana Dashboards

### SMS Platform Dashboard
**URL**: http://localhost:3000/d/sms-platform

**Panels**:
- **Messages Sent Rate** - Real-time success rate
- **Messages Failed Rate** - Real-time failure rate  
- **Success Rate** - Overall success percentage
- **Total Messages** - Cumulative message count
- **HTTP Request Rate** - API request volume
- **HTTP Response Time** - API latency percentiles
- **JVM Memory Usage** - Application memory consumption
- **Kafka Consumer Lag** - Message processing lag

### Key Queries

**Success Rate**:
```promql
sum(rate(sms_sent_total[1m])) / (sum(rate(sms_sent_total[1m])) + sum(rate(sms_failed_total[1m]))) * 100
```

**Message Throughput**:
```promql
sum(rate(sms_send_attempts_total[1m]))
```

**API Latency (95th percentile)**:
```promql
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{job="sms-services"}[1m])) by (le))
```

## 🔍 Monitoring Use Cases

### 1. Real-time Operations Monitoring
- Monitor message success/failure rates
- Track API response times
- Alert on high error rates

### 2. Capacity Planning
- Track message volume trends
- Monitor resource utilization
- Plan scaling decisions

### 3. Troubleshooting
- Identify performance bottlenecks
- Correlate failures with system metrics
- Debug Kafka consumer lag issues

## 🚨 Alerting (Future Enhancement)

### Recommended Alerts

**High Failure Rate**:
```promql
sum(rate(sms_failed_total[5m])) / (sum(rate(sms_sent_total[5m])) + sum(rate(sms_failed_total[5m]))) > 0.1
```

**High API Latency**:
```promql
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le)) > 1.0
```

**Kafka Consumer Lag**:
```promql
kafka_consumer_lag_max > 1000
```

## 🔧 Configuration

### Prometheus Configuration
**File**: `monitoring/prometheus.yml`

```yaml
scrape_configs:
  - job_name: 'sms-services'
    metrics_path: '/q/metrics'
    static_configs:
      - targets: ['sms-service:8080', 'processor-service:8080']
```

### Custom Metrics in Code

**Add Counters**:
```java
@Inject
MeterRegistry meterRegistry;

// Increment counter
meterRegistry.counter("custom_metric_total").increment();

// Counter with tags
meterRegistry.counter("messages_processed", "status", "success").increment();
```

**Add Timers**:
```java
@Timed(value = "operation_duration", description = "Operation timing")
public void someOperation() {
    // method body
}
```

**Add Gauges**:
```java
Gauge.builder("queue_size")
    .description("Current queue size")
    .register(meterRegistry, this, obj -> getCurrentQueueSize());
```

## 📱 Mobile/Remote Access

### Grafana Mobile App
1. Install Grafana mobile app
2. Connect to: http://your-server:3000
3. Login with admin/admin
4. Access dashboards on-the-go

### Prometheus API
```bash
# Query current metrics
curl "http://localhost:9090/api/v1/query?query=sms_sent_total"

# Query range
curl "http://localhost:9090/api/v1/query_range?query=rate(sms_sent_total[1m])&start=2024-01-01T00:00:00Z&end=2024-01-01T01:00:00Z&step=15s"
```

## 🐳 Production Deployment

### Environment Variables
```bash
# Prometheus retention
PROMETHEUS_RETENTION_TIME=15d
PROMETHEUS_RETENTION_SIZE=50GB

# Grafana security
GF_SECURITY_ADMIN_PASSWORD=secure_password
GF_SECURITY_SECRET_KEY=your_secret_key
GF_INSTALL_PLUGINS=grafana-piechart-panel
```

### Docker Compose Production
```yaml
prometheus:
  image: prom/prometheus:v2.53
  command:
    - '--config.file=/etc/prometheus/prometheus.yml'
    - '--storage.tsdb.path=/prometheus'
    - '--storage.tsdb.retention.time=15d'
    - '--web.enable-lifecycle'
    - '--web.enable-admin-api'
```

## 🧪 Testing Metrics

### Generate Test Load
```bash
# Send test messages
for i in {1..100}; do
  curl -X POST http://localhost:8080/v1/messages \
    -H "Content-Type: application/json" \
    -d "{\"sender\": \"+123456789$i\", \"recipient\": \"+987654321$i\", \"text\": \"Test message $i\"}"
  sleep 0.1
done
```

### Verify Metrics
```bash
# Check Prometheus targets
curl http://localhost:9090/api/v1/targets

# Query business metrics
curl "http://localhost:9090/api/v1/query?query=sms_send_attempts_total"

# Check Grafana health
curl http://localhost:3000/api/health
```

## 📚 Best Practices

### 1. Metric Naming
- Use descriptive names: `sms_sent_total` vs `messages`
- Include units: `duration_seconds`, `size_bytes`
- Use consistent prefixes: `sms_*`, `http_*`

### 2. Cardinality Management
- Avoid high-cardinality labels (user IDs, message content)
- Use bounded label values (status: success/failed)
- Limit label combinations

### 3. Dashboard Design
- Group related metrics together
- Use consistent time ranges
- Add meaningful descriptions
- Include SLA/target lines

### 4. Performance Impact
- Metrics collection adds ~1-2% overhead
- Scrape intervals: 15s for apps, 1m for infrastructure
- Monitor Prometheus resource usage

## 🔗 External Integrations

### Alertmanager (Future)
```yaml
# alertmanager.yml
route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'web.hook'
```

### Export to External Systems
- **DataDog**: Use prometheus-to-datadog bridge
- **New Relic**: Prometheus remote write
- **AWS CloudWatch**: CloudWatch agent integration

## 🆘 Troubleshooting

### Common Issues

**Metrics Not Appearing**:
```bash
# Check if metrics endpoint is accessible
curl http://localhost:8080/q/metrics

# Verify Prometheus can scrape targets
curl http://localhost:9090/api/v1/targets
```

**Grafana Dashboard Empty**:
```bash
# Check Prometheus data source
curl http://localhost:3000/api/datasources

# Verify Prometheus has data
curl "http://localhost:9090/api/v1/query?query=up"
```

**High Cardinality Warning**:
```bash
# Check series count
curl http://localhost:9090/api/v1/label/__name__/values | jq length

# Top metrics by series count
curl -s "http://localhost:9090/api/v1/query?query=topk(10,{__name__=~\".+\"})"
```

## 📈 Monitoring Maturity

### Level 1: Basic (Current)
- ✅ Application metrics collection
- ✅ System dashboards
- ✅ Manual monitoring

### Level 2: Intermediate (Future)
- [ ] Automated alerting
- [ ] SLA monitoring
- [ ] Log aggregation

### Level 3: Advanced (Future)
- [ ] Distributed tracing
- [ ] Anomaly detection
- [ ] Predictive scaling

---

This monitoring setup provides enterprise-grade observability for the SMS messaging platform, enabling proactive monitoring, troubleshooting, and capacity planning.
