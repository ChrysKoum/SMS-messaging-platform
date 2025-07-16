# Architecture Decision Records (ADR)

This directory contains Architecture Decision Records (ADRs) that document important architectural decisions made during the development of the SMS Messaging Platform.

## ADR Format

Each ADR follows this structure:
- **Title**: Brief description of the decision
- **Status**: Proposed, Accepted, Deprecated, or Superseded
- **Context**: The issue that motivated this decision
- **Decision**: The change we're proposing or have agreed to implement
- **Consequences**: What becomes easier or more difficult after this change

## Index of ADRs

| # | Title | Status | Date |
|---|-------|--------|------|
| [001](001-microservices-architecture.md) | Use Microservices Architecture | Accepted | 2025-07-16 |
| [002](002-kafka-message-broker.md) | Use Apache Kafka as Message Broker | Accepted | 2025-07-16 |
| [003](003-quarkus-framework.md) | Use Quarkus as Java Framework | Accepted | 2025-07-16 |
| [004](004-postgresql-database.md) | Use PostgreSQL as Primary Database | Accepted | 2025-07-16 |
| [005](005-docker-containerization.md) | Use Docker for Containerization | Accepted | 2025-07-16 |
| [006](006-rest-api-design.md) | RESTful API Design Principles | Accepted | 2025-07-16 |
| [007](007-async-processing-pattern.md) | Asynchronous Message Processing Pattern | Accepted | 2025-07-16 |

## Quick Navigation

### Core Architecture
- [Microservices Architecture](001-microservices-architecture.md)
- [Message Broker Selection](002-kafka-message-broker.md)
- [Asynchronous Processing](007-async-processing-pattern.md)

### Technology Choices
- [Java Framework](003-quarkus-framework.md)
- [Database Selection](004-postgresql-database.md)
- [Containerization](005-docker-containerization.md)

### API Design
- [REST API Design](006-rest-api-design.md)

## Creating New ADRs

When making significant architectural decisions:

1. Create a new file: `XXX-decision-title.md`
2. Use the next available number
3. Follow the standard ADR template
4. Update this index
5. Submit for team review

## ADR Template

```markdown
# ADR-XXX: [Decision Title]

## Status
[Proposed | Accepted | Deprecated | Superseded by ADR-XXX]

## Context
[Describe the issue that motivates this decision]

## Decision
[Describe the change we're proposing or have agreed to implement]

## Consequences
### Positive
- [What becomes easier]

### Negative  
- [What becomes more difficult]

### Neutral
- [Other implications]
```
