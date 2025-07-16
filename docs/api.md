# API Documentation

## Overview

The SMS Messaging Platform provides a RESTful API for sending SMS messages and managing message operations. The API follows REST principles and returns JSON responses.

**Base URL**: `http://localhost:8080`

**API Version**: `v1`

**Content-Type**: `application/json`

## Authentication

Currently, the API endpoints are publicly accessible. For production deployment, implement JWT or API key authentication.

## Interactive Documentation

- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **OpenAPI Spec**: http://localhost:8080/q/openapi

## Core Endpoints

### 1. Send SMS Message

**Endpoint**: `POST /v1/messages`

Send a new SMS message for processing.

**Request Body**:
```json
{
  "sender": "+1234567890",
  "recipient": "+1987654321",
  "text": "Your message content here"
}
```

**Request Validation**:
- `sender`: Required, international phone number format (e.g., +1234567890)
- `recipient`: Required, international phone number format (e.g., +1234567890)
- `text`: Required, 1-1600 characters, must contain non-whitespace content

**Response**: `202 Accepted`
```json
{
  "id": "be9d4804-233f-4e54-92c3-16a7228dd800",
  "sender": "+1234567890",
  "recipient": "+1987654321",
  "text": "Your message content here",
  "status": "PENDING",
  "created_at": "2025-07-16T19:22:09",
  "updated_at": "2025-07-16T19:22:09"
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/v1/messages \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "+1234567890",
    "recipient": "+1987654321",
    "text": "Hello World!"
  }'
```

### 2. Get Message by ID

**Endpoint**: `GET /v1/messages/{messageId}`

Retrieve a specific message by its UUID.

**Path Parameters**:
- `messageId`: UUID of the message

**Response**: `200 OK`
```json
{
  "id": "be9d4804-233f-4e54-92c3-16a7228dd800",
  "sender": "+1234567890",
  "recipient": "+1987654321",
  "text": "Hello World!",
  "status": "SENT",
  "created_at": "2025-07-16T19:22:09",
  "updated_at": "2025-07-16T19:22:12"
}
```

**Example**:
```bash
curl http://localhost:8080/v1/messages/be9d4804-233f-4e54-92c3-16a7228dd800
```

### 3. List All Messages

**Endpoint**: `GET /v1/messages`

Get a paginated list of all messages.

**Query Parameters**:
- `page`: Page number (0-based, default: 0)
- `size`: Page size (1-100, default: 20)
- `status`: Filter by status (PENDING, SENT, FAILED)

**Response**: `200 OK`
```json
{
  "messages": [
    {
      "id": "be9d4804-233f-4e54-92c3-16a7228dd800",
      "sender": "+1234567890",
      "recipient": "+1987654321",
      "text": "Hello World!",
      "status": "SENT",
      "created_at": "2025-07-16T19:22:09",
      "updated_at": "2025-07-16T19:22:12"
    }
  ],
  "total_count": 1,
  "page": 0,
  "page_size": 20,
  "total_pages": 1
}
```

**Example**:
```bash
# Get first page of messages
curl "http://localhost:8080/v1/messages?page=0&size=10"

# Filter by status
curl "http://localhost:8080/v1/messages?status=SENT"
```

### 4. Get User Messages

**Endpoint**: `GET /v1/users/{userId}/messages`

Get messages for a specific user (phone number).

**Path Parameters**:
- `userId`: User phone number in international format

**Query Parameters**:
- `page`: Page number (0-based, default: 0)
- `size`: Page size (1-100, default: 20)
- `status`: Filter by status (PENDING, SENT, FAILED)

**Response**: `200 OK` (same format as list messages)

**Example**:
```bash
curl "http://localhost:8080/v1/users/+1234567890/messages"
```

### 5. Get Message Statistics

**Endpoint**: `GET /v1/messages/stats`

Get aggregated statistics about messages.

**Response**: `200 OK`
```json
{
  "total_messages": 150,
  "pending_messages": 5,
  "sent_messages": 140,
  "failed_messages": 5,
  "success_rate": 93.33
}
```

**Example**:
```bash
curl http://localhost:8080/v1/messages/stats
```

### 6. Get Failed Messages

**Endpoint**: `GET /v1/messages/failed`

Get a list of failed messages for analysis.

**Query Parameters**:
- `page`: Page number (0-based, default: 0)
- `size`: Page size (1-100, default: 20)

**Response**: `200 OK` (same format as list messages)

**Example**:
```bash
curl "http://localhost:8080/v1/messages/failed?page=0&size=10"
```

## Internal Endpoints

### Delivery Report Callback

**Endpoint**: `POST /v1/internal/delivery-report`

Internal endpoint for processor service to report delivery status.

**Request Body**:
```json
{
  "message_id": "be9d4804-233f-4e54-92c3-16a7228dd800",
  "status": "SENT",
  "processed_at": "2025-07-16T19:22:12"
}
```

## Status Codes

| Code | Description |
|------|-------------|
| 200 | OK - Request successful |
| 202 | Accepted - Message accepted for processing |
| 400 | Bad Request - Invalid input data |
| 404 | Not Found - Message not found |
| 422 | Unprocessable Entity - Validation failed |
| 500 | Internal Server Error - Server error |

## Message Status Values

| Status | Description |
|--------|-------------|
| PENDING | Message queued for processing |
| SENT | Message successfully sent |
| FAILED | Message delivery failed |

## Error Response Format

All error responses follow RFC 7807 format:

```json
{
  "title": "Constraint Violation",
  "status": 400,
  "violations": [
    {
      "field": "sendMessage.request.recipient",
      "message": "Invalid recipient phone number format. Use international format (e.g., +1234567890)"
    }
  ]
}
```

## Rate Limiting

Currently no rate limiting is implemented. For production, consider implementing:
- Per-IP rate limiting
- Per-user rate limiting
- API key-based quotas

## Health and Monitoring

### Health Check
**Endpoint**: `GET /q/health`

Returns service health status including database and Kafka connectivity.

### Metrics
**Endpoint**: `GET /q/metrics`

Prometheus-format metrics including:
- `sms_sent_total` - Total sent messages
- `sms_failed_total` - Total failed messages  
- `sms_send_duration` - Message send duration
- `sms_callback_duration` - Callback processing duration

## Client Examples

### JavaScript/Node.js
```javascript
const response = await fetch('http://localhost:8080/v1/messages', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    sender: '+1234567890',
    recipient: '+1987654321',
    text: 'Hello from JavaScript!'
  })
});

const message = await response.json();
console.log('Message ID:', message.id);
```

### Python
```python
import requests

response = requests.post('http://localhost:8080/v1/messages', 
  json={
    'sender': '+1234567890',
    'recipient': '+1987654321',
    'text': 'Hello from Python!'
  }
)

message = response.json()
print(f"Message ID: {message['id']}")
```

### Java
```java
// Using OkHttp
RequestBody body = RequestBody.create(
  MediaType.parse("application/json"),
  "{\"sender\":\"+1234567890\",\"recipient\":\"+1987654321\",\"text\":\"Hello from Java!\"}"
);

Request request = new Request.Builder()
  .url("http://localhost:8080/v1/messages")
  .post(body)
  .build();

Response response = client.newCall(request).execute();
```

## Testing the API

### Using curl
```bash
# Send a message
MESSAGE_ID=$(curl -s -X POST http://localhost:8080/v1/messages \
  -H "Content-Type: application/json" \
  -d '{"sender":"+1234567890","recipient":"+1987654321","text":"Test message"}' \
  | jq -r '.id')

# Check message status
curl http://localhost:8080/v1/messages/$MESSAGE_ID

# Wait a moment for processing, then check again
sleep 2
curl http://localhost:8080/v1/messages/$MESSAGE_ID
```

### Using Postman
1. Import the OpenAPI specification from `http://localhost:8080/q/openapi`
2. Create a new collection from the imported spec
3. Set the base URL to `http://localhost:8080`
4. Test endpoints with the provided examples

## Troubleshooting

### Common Issues

**400 Bad Request - Phone Number Format**
- Ensure phone numbers start with `+` and country code
- Use international format: `+1234567890`
- Avoid spaces, dashes, or parentheses

**500 Internal Server Error**
- Check if Kafka is running: `docker-compose ps`
- Check if PostgreSQL is accessible
- Review application logs: `docker-compose logs sms-service`

**Message stuck in PENDING**
- Verify processor service is running
- Check Kafka connectivity
- Review processor service logs: `docker-compose logs processor-service`

For more troubleshooting, see the [Deployment Guide](deployment.md).
