# MedEnt Agent Platform — Architecture

## Overview

MedEnt Agent Platform is a sellable, enterprise-grade agentic AI low-code/no-code solution designed to orchestrate medical entrance examinations (NEET-style) for **25 lakh (2.5 million) concurrent students** across four subjects: Physics, Chemistry, Botany, and Zoology.

## System Components

### 1. Agent Orchestrator (Java 21)

The core brain of the platform. Built with Spring Boot 3.3 and Java 21's latest features:

| Feature | Usage |
|---------|-------|
| **Virtual Threads** | Handle millions of concurrent exam sessions with minimal memory overhead |
| **Structured Concurrency** | Safe parallel workflow node execution with automatic cancellation |
| **Sealed Classes** | Type-safe agent node hierarchy (Trigger, Reason, Tool, Decision, Action) |
| **Pattern Matching** | Exhaustive switch on agent node types during workflow execution |
| **Records** | Immutable domain models (Student, ExamSession, AgentWorkflow, Alert) |
| **ZGC Generational** | Low-latency garbage collection for 2.5M concurrent sessions |

**Deep Java Library (DJL)** provides on-JVM ML inference for subject classification and reasoning confidence scoring.

### 2. Analytics API (FastAPI + Keras)

Python-based analytics service providing:

- **Performance Predictor** — Keras dense neural network predicting NEET scores
- **Subject Classifier** — Identifies weakest/strongest subjects per student
- **Dropout Risk Model** — Early warning for students at risk of abandoning exam
- Real-time dashboards, geographic distribution, time-series analytics
- Executive summary reports for administrators

### 3. Message Queue (RabbitMQ)

Event-driven architecture with topic exchange `medent.agent.events`:

- `exam.*` — Exam lifecycle events (start, heartbeat, submit)
- `analytics.*` — Analytics pipeline events
- `agent.action` — Agent action execution events

### 4. Data Stores

| Store | Purpose | Scaling Strategy |
|-------|---------|-----------------|
| **MongoDB** | Students, exam sessions, workflows, alerts | Sharded by student ID hash |
| **Redis** | Session cache, real-time counters, rate limiting | Redis Cluster with LRU eviction |

### 5. Frontends (React + TypeScript)

| Portal | Port | Audience | Key Features |
|--------|------|----------|-------------|
| Admin | 3001 | Administrators | Live student monitoring, analytics, workflow management |
| Student | 3002 | Exam takers | Secure exam interface with 4-subject navigation |
| Observability | 3003 | SRE/Ops | Critical alert monitoring, system health metrics |

### 6. Security (AWS)

- **Cognito** — User pool with ADMIN, STUDENT, OBSERVABILITY groups
- **JWT + OAuth 2.0** — Authorization code flow with 1-hour access tokens
- **AWS WAF** — Rate limiting (2000 req/IP), SQLi protection, bad input filtering
- **AWS Shield Advanced** — DDoS protection on CloudFront distribution

## Agent Workflow Engine

The low-code/no-code workflow builder supports five node types connected by directed edges:

```
[TRIGGER] → [REASON] → [TOOL] → [DECISION] → [ACTION]
                                    ├─ true  → [ACTION: Continue]
                                    └─ false → [ACTION: Alert]
```

Workflows are stored as JSON documents in MongoDB and executed by the Java 21 engine using virtual threads for parallel fan-out.

### Built-in Agent Tools

| Tool | Description |
|------|------------|
| `fetch_student_profile` | Retrieve student data from MongoDB |
| `check_exam_integrity` | Proctoring integrity checks |
| `calculate_score` | Real-time score computation |
| `send_alert` | Publish alert to observability portal |
| `recommend_study_plan` | Post-exam study recommendations |
| `proctor_check` | Face detection and tab-switch monitoring |
| `time_warning` | Time-remaining warnings |

## Scalability Design

### Thread Safety
- Virtual threads (Java 21) — lightweight, no thread pool exhaustion
- `ConcurrentHashMap` for execution counters
- MongoDB atomic operations for session updates
- Redis `SETEX` for session caching with TTL

### Process Safety
- RabbitMQ durable queues survive broker restarts
- MongoDB replica sets for data durability
- Health checks on all Docker services
- Graceful shutdown via Spring Boot lifecycle

### Load Balancing
- Nginx `least_conn` upstream for agent and analytics APIs
- CloudFront CDN for static portal assets
- Horizontal pod autoscaling in production (EKS)

### Memory Safety
- ZGC generational collector with 512MB–2GB heap bounds
- Redis `maxmemory 512mb` with `allkeys-lru` eviction
- Session TTL of 4 hours in Redis
- Bounded virtual thread pools (no platform thread leaks)

## Capacity Planning (25 Lakh Students)

| Metric | Target |
|--------|--------|
| Concurrent sessions | 2,500,000 |
| Heartbeats/minute | 83,333 |
| API requests/second (peak) | 50,000 |
| MongoDB documents | ~10M (sessions + students) |
| Redis memory | ~4GB (session cache) |
| RabbitMQ messages/second | 5,000 |

## Data Flow

```
Student Login (Cognito JWT)
    → Student Portal
    → POST /api/v1/student/exam/start
    → Agent Orchestrator
        → MongoDB (persist session)
        → Redis (cache session)
        → RabbitMQ (exam.started event)
        → Agent Workflow Engine (proctoring workflow)
            → DJL (subject classification)
            → Tools (proctor_check, time_warning)
            → Decision (integrity check)
            → Action (alert or continue)
    → Analytics API (Keras predictions)
    → Admin Portal (live monitoring)
    → Observability Portal (critical alerts)
```
