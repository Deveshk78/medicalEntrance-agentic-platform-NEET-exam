# Deployment Guide

## Prerequisites

- Docker Desktop 4.x+ with Compose V2
- Java 21 JDK (for local Java development)
- Node.js 20+ (for local portal development)
- Python 3.12+ (for local analytics development)
- Terraform 1.5+ (for AWS infrastructure)

## Local Development (Docker Compose)

```bash
# Clone and enter project
cd C:\Users\DeveshKumar(HtH)\Projects\medent-agent-platform

# Copy environment
cp .env.example .env

# Start all services
docker compose up -d --build

# Verify services
docker compose ps
```

### Service URLs (Local)

| Service | URL |
|---------|-----|
| Admin Portal | http://localhost:3001 |
| Student Portal | http://localhost:3002 |
| Observability Portal | http://localhost:3003 |
| Agent API (via LB) | http://localhost:80 |
| Agent API (direct) | http://localhost:8080 |
| Analytics API | http://localhost:8000 |
| RabbitMQ Management | http://localhost:15672 (medent/medent_secret) |
| MongoDB | localhost:27017 |
| Redis | localhost:6379 |

## AWS Production Deployment

### 1. Infrastructure (Terraform)

```bash
cd infrastructure/aws
terraform init
terraform plan -var="domain_name=exam.medent.in"
terraform apply -var="domain_name=exam.medent.in"
```

This provisions:
- Cognito User Pool with groups
- WAF Web ACL with rate limiting and managed rules
- Shield Advanced protection
- CloudFront distribution

### 2. Container Deployment (EKS)

```bash
# Build and push images
docker build -t medent/agent-orchestrator ./services/agent-orchestrator
docker build -t medent/analytics-api ./services/analytics-api
docker build -t medent/admin-portal ./portals/admin
docker build -t medent/student-portal ./portals/student
docker build -t medent/observability-portal ./portals/observability

# Deploy to EKS with HPA
kubectl apply -f infrastructure/k8s/
```

### 3. Environment Variables (Production)

Set in AWS Secrets Manager or EKS ConfigMaps:

```
COGNITO_USER_POOL_ID=<from terraform output>
COGNITO_CLIENT_ID=<from terraform output>
COGNITO_JWKS_URL=<from terraform output>
MONGO_URI=mongodb+srv://...
REDIS_HOST=medent-redis.xxxxx.cache.amazonaws.com
```

## Scaling Configuration

### Agent Orchestrator
- Min replicas: 10
- Max replicas: 100
- HPA target: 70% CPU
- JVM: `-XX:+UseZGC -Xms2g -Xmx4g`

### Analytics API
- Min replicas: 5
- Max replicas: 50
- HPA target: 60% CPU

### MongoDB
- M40+ cluster for 25L students
- Sharded by `studentId` hash
- 3-node replica set minimum

### Redis
- ElastiCache cluster mode enabled
- 3 shards, 2 replicas each
- `maxmemory-policy: allkeys-lru`

## Health Checks

```bash
# Agent Orchestrator
curl http://localhost:8080/api/v1/public/health

# Analytics API
curl http://localhost:8000/health

# Full stack check
curl http://localhost:80/api/v1/public/health
```

## Monitoring

- Spring Boot Actuator: `/actuator/health`, `/actuator/prometheus`
- FastAPI: `/health`
- RabbitMQ: Management UI on port 15672
- Observability Portal: http://localhost:3003

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Agent won't start | Check MongoDB/Redis/RabbitMQ health: `docker compose ps` |
| Analytics slow | Keras model training on startup takes ~30s; wait for `/health` |
| Portal blank | Check browser console; ensure API URLs are reachable |
| Auth errors | In dev mode, security is disabled (`spring.profiles.active=dev`) |
