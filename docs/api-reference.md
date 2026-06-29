# API Reference

Base URLs:
- **Agent Orchestrator**: `http://localhost:8080`
- **Analytics API**: `http://localhost:8000`

## Authentication

All endpoints (except `/api/v1/public/**`) require a JWT Bearer token from AWS Cognito.

```
Authorization: Bearer <cognito_access_token>
```

### Cognito Groups → Roles

| Group | Role | Access |
|-------|------|--------|
| ADMIN | ROLE_ADMIN | Admin portal, all APIs |
| STUDENT | ROLE_STUDENT | Student portal, exam APIs |
| OBSERVABILITY | ROLE_OBSERVABILITY | Observability portal |

---

## Agent Orchestrator API

### Public

#### `GET /api/v1/public/health`
Health check. No auth required.

**Response:**
```json
{ "status": "UP", "service": "agent-orchestrator" }
```

### Student Exam

#### `POST /api/v1/student/exam/start`
Start a new exam session.

**Request:**
```json
{ "studentId": "s1" }
```

**Response:** `ExamSession` object

#### `POST /api/v1/student/exam/{sessionId}/heartbeat`
Send heartbeat to keep session alive.

**Response:** Updated `ExamSession`

### Admin

#### `GET /api/v1/admin/dashboard/stats`
Dashboard statistics.

**Response:**
```json
{
  "activeStudents": 1847293,
  "totalRegistered": 2500000,
  "criticalAlerts": 23,
  "capacity": 2500000,
  "utilizationPercent": 73.9,
  "subjects": ["PHYSICS", "CHEMISTRY", "BOTANY", "ZOOLOGY"]
}
```

#### `GET /api/v1/admin/exam/sessions/active`
List all active exam sessions.

#### `GET /api/v1/admin/workflows`
List active agent workflows.

#### `POST /api/v1/admin/workflows`
Create or update a workflow.

#### `POST /api/v1/admin/workflows/{id}/execute`
Execute a workflow with context.

**Request:**
```json
{
  "studentId": "s1",
  "subject": "PHYSICS",
  "accuracy": 0.65,
  "timeRemaining": 3600
}
```

### Observability

#### `GET /api/v1/observability/alerts?unacknowledgedOnly=true`
List alerts.

#### `GET /api/v1/observability/alerts/critical`
List critical alerts only.

#### `POST /api/v1/observability/alerts`
Create a new alert.

#### `PATCH /api/v1/observability/alerts/{id}/acknowledge`
Acknowledge an alert.

#### `GET /api/v1/observability/metrics`
Observability metrics summary.

---

## Analytics API

### `GET /health`
Service health check.

### `GET /api/v1/analytics/dashboard`
Real-time analytics dashboard data.

### `GET /api/v1/analytics/subjects`
Per-subject analytics (Physics, Chemistry, Botany, Zoology).

### `GET /api/v1/analytics/geographic`
State-wise student distribution and scores.

### `GET /api/v1/analytics/time-series`
Hourly activity time series.

### `POST /api/v1/predictions/performance`
Predict student NEET performance.

**Request:**
```json
{
  "student_id": "s1",
  "physics_accuracy": 0.62,
  "chemistry_accuracy": 0.58,
  "botany_accuracy": 0.65,
  "zoology_accuracy": 0.60,
  "time_spent_hours": 2.5,
  "questions_answered": 120,
  "mock_tests_taken": 8
}
```

### `POST /api/v1/predictions/weak-subjects`
Classify weakest and strongest subjects.

### `POST /api/v1/predictions/dropout-risk`
Predict exam dropout risk.

### `GET /api/v1/reports/executive-summary`
Executive summary report.

### `GET /api/v1/reports/agent-performance`
Agent workflow performance metrics.
