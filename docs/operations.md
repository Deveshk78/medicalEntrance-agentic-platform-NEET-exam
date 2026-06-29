# Operations Runbook

## Daily Checks

1. Verify all services healthy: `docker compose ps`
2. Check observability portal for unacknowledged critical alerts
3. Review admin dashboard utilization (should be < 90% of 25L capacity)
4. Monitor RabbitMQ queue depths via management UI

## Incident Response

### High CPU on Agent Orchestrator
1. Check active session count in admin dashboard
2. Scale horizontally: `docker compose up -d --scale agent-orchestrator=3`
3. Verify ZGC is active: check JVM flags in container logs

### MongoDB Connection Errors
1. `docker compose logs mongo`
2. Verify disk space: `docker system df`
3. Restart: `docker compose restart mongo`

### RabbitMQ Queue Backlog
1. Open http://localhost:15672
2. Check queue `medent.exam.events` message count
3. If > 10,000: scale analytics consumers

### Critical Alert: Tab Switch Detected
1. Observability portal shows alert with student ID
2. Agent workflow automatically flags session
3. Admin reviews in student monitoring table
4. Acknowledge alert after review

## Scaling Procedures

### Horizontal Scaling (Docker)
```bash
docker compose up -d --scale agent-orchestrator=5 --scale analytics-api=3
```

### Production (EKS)
```bash
kubectl scale deployment agent-orchestrator --replicas=50
kubectl scale deployment analytics-api --replicas=20
```

## Backup

- MongoDB: `mongodump --uri=mongodb://localhost:27017/medent`
- Redis: `redis-cli BGSAVE`
- Workflows: Exported via `/api/v1/admin/workflows`

## Log Locations

| Service | Command |
|---------|---------|
| Agent Orchestrator | `docker compose logs agent-orchestrator` |
| Analytics API | `docker compose logs analytics-api` |
| RabbitMQ | `docker compose logs rabbitmq` |
| All services | `docker compose logs -f` |

## Performance Benchmarks

| Metric | Target | Alert Threshold |
|--------|--------|----------------|
| API latency (p99) | < 100ms | > 500ms |
| Heartbeat processing | < 50ms | > 200ms |
| Workflow execution | < 200ms | > 1000ms |
| ML prediction | < 300ms | > 2000ms |
| System uptime | 99.99% | < 99.9% |
