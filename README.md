# MedEnt Agent Platform

**Agentic AI Low-Code/No-Code Platform for Medical Entrance Examinations**

A production-grade, scalable platform supporting **25 lakh (2.5M) students** taking NEET-style medical entrance exams across Physics, Chemistry, Botany, and Zoology.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        AWS CloudFront + Shield + WAF                        │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
        ┌─────────────────────────────┼─────────────────────────────┐
        ▼                             ▼                             ▼
┌───────────────┐           ┌─────────────────┐           ┌─────────────────┐
│ Admin Portal  │           │ Student Portal  │           │ Observability   │
│  (React/TS)   │           │  (React/TS)     │           │    Portal       │
└───────┬───────┘           └────────┬────────┘           └────────┬────────┘
        │                            │                             │
        └────────────────────────────┼─────────────────────────────┘
                                     ▼
                    ┌────────────────────────────────┐
                    │   Agent Orchestrator (Java 21) │
                    │  Virtual Threads · DJL · JWT   │
                    └───────────────┬────────────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              ▼                     ▼                     ▼
        ┌──────────┐         ┌──────────┐         ┌──────────────┐
        │ RabbitMQ │         │ MongoDB  │         │    Redis     │
        └──────────┘         └──────────┘         └──────────────┘
                                    │
                                    ▼
                    ┌────────────────────────────────┐
                    │  Analytics API (FastAPI/Keras) │
                    └────────────────────────────────┘
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Agent Orchestration | Java 21, Spring Boot 3.3, Virtual Threads, Structured Concurrency |
| ML Inference | Deep Java Library (DJL), Python Keras |
| Message Queue | RabbitMQ |
| Databases | MongoDB (persistent), Redis (cache/sessions) |
| Analytics | FastAPI, Keras, NumPy, Pandas |
| Frontends | React 18, TypeScript, Vite |
| Auth | AWS Cognito (JWT + OAuth 2.0) |
| Security | AWS Shield, AWS WAF |
| Infrastructure | Docker Compose, Terraform |

## Quick Start

```bash
# 1. Copy environment
cp .env.example .env

# 2. Start infrastructure + all services
docker compose up -d --build

# 3. Access portals
# Admin:          http://localhost:3001
# Student:        http://localhost:3002
# Observability:  http://localhost:3003
# Agent API:      http://localhost:8080
# Analytics API:  http://localhost:8000
# RabbitMQ UI:    http://localhost:15672
```

## Documentation

- [Architecture](docs/architecture.md)
- [API Reference](docs/api-reference.md)
- [Security & Auth](docs/security.md)
- [Analytics](docs/analytics.md)
- [Deployment](docs/deployment.md)
- [Agent Workflow Builder](docs/agent-workflows.md)
- [Operations Runbook](docs/operations.md)

## Project Structure

```
medent-agent-platform/
├── services/
│   ├── agent-orchestrator/    # Java 21 agent engine
│   └── analytics-api/         # FastAPI + Keras analytics
├── portals/
│   ├── admin/                 # Admin dashboard
│   ├── student/               # Student exam portal
│   └── observability/         # Critical alerts portal
├── infrastructure/
│   └── aws/                   # Terraform (Cognito, WAF, Shield)
├── shared/
│   └── workflow-schema/       # Low-code workflow JSON schema
├── docs/                      # Full documentation
└── scripts/                   # Startup & utility scripts
```

## License

Proprietary — MedEnt Agent Platform © 2026
