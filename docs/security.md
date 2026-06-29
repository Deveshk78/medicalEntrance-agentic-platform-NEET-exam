# Security

## Authentication Architecture

```
┌──────────────┐     OAuth 2.0      ┌──────────────┐     JWT      ┌──────────────┐
│   Browser    │ ──── Code Flow ──→ │ AWS Cognito  │ ──────────→ │  API Gateway │
│  (Portals)   │ ←── Tokens ────── │  User Pool   │             │  + Services  │
└──────────────┘                    └──────────────┘             └──────────────┘
```

## AWS Cognito Configuration

### User Pool
- Email-based usernames with auto-verification
- Custom attribute: `roll_number` for NEET roll numbers
- Password policy: 12+ chars, mixed case, numbers, symbols

### User Groups
| Group | Permissions |
|-------|------------|
| ADMIN | Full platform access, workflow management, all analytics |
| STUDENT | Exam portal, own session data only |
| OBSERVABILITY | Alert monitoring, system metrics, read-only |

### Token Configuration
- Access token: 1 hour validity
- ID token: 1 hour validity
- Refresh token: 30 days validity
- OAuth flows: Authorization Code + Implicit

### JWT Validation (Java Orchestrator)
```java
// Production: Cognito JWKS endpoint
NimbusJwtDecoder.withJwkSetUri(cognitoJwksUrl).build();

// Claims used:
// - sub: Cognito user ID
// - cognito:groups: [ADMIN, STUDENT, OBSERVABILITY]
// - email: Student email
```

## AWS WAF Rules

| Rule | Priority | Action | Purpose |
|------|----------|--------|---------|
| RateLimitRule | 1 | Block | 2000 requests/IP — prevents exam API abuse |
| CommonRuleSet | 2 | Managed | OWASP Top 10 protection |
| KnownBadInputs | 3 | Managed | Blocks known malicious payloads |
| SQLiRuleSet | 4 | Managed | SQL injection prevention |

## AWS Shield Advanced

- DDoS protection on CloudFront distribution
- Automatic attack detection and mitigation
- 24/7 DDoS Response Team (DRT) access
- Cost protection against scaling attacks

## Application Security

### Thread Safety
- All shared state uses `ConcurrentHashMap` or atomic types
- MongoDB operations are atomic at document level
- Redis operations are single-threaded per connection

### Input Validation
- Spring `@Valid` on all request bodies
- Pydantic models on FastAPI endpoints
- Workflow JSON validated against schema before execution

### Session Security
- Exam sessions cached in Redis with 4-hour TTL
- Heartbeat required every 30 seconds
- IP address logged for integrity checks
- Tab-switch detection via proctoring agent

### Network Security
- All inter-service communication within Docker network
- CloudFront enforces HTTPS
- Nginx reverse proxy with `X-Forwarded-For` headers
- No direct database exposure to internet

## Compliance Considerations

- Student PII encrypted at rest (MongoDB encryption)
- Audit trail via RabbitMQ event log
- Exam session recordings for dispute resolution
- Role-based access control (RBAC) via Cognito groups
