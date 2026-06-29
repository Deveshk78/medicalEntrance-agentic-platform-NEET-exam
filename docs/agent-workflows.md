# Agent Workflows — Low-Code / No-Code Guide

## Overview

MedEnt's agent workflow engine enables drag-and-drop configuration of AI agents that **reason**, **use tools**, **make decisions**, and **take actions** — without writing code.

## Node Types

### TRIGGER
Entry point for workflow execution. Fires on events:
- `exam.start` — Student begins exam
- `exam.heartbeat` — Periodic heartbeat
- `exam.submit` — Student submits exam
- `question.answer` — Student answers a question

### REASON
AI reasoning step using DJL inference. Analyzes context and produces confidence scores.

**Config:**
```json
{
  "prompt": "Analyze student exam behavior patterns for integrity violations"
}
```

### TOOL
Invokes a registered agent tool.

**Available Tools:**
| Tool | Description |
|------|------------|
| `fetch_student_profile` | Get student data |
| `check_exam_integrity` | Run integrity checks |
| `calculate_score` | Compute current score |
| `send_alert` | Send alert to observability |
| `recommend_study_plan` | Generate study plan |
| `proctor_check` | Face/tab monitoring |
| `time_warning` | Send time warning |

### DECISION
Conditional branching based on context evaluation.

**Conditions:**
- `accuracy > 0.6` — Student accuracy threshold
- `time_remaining < 300` — Less than 5 minutes left
- Custom expressions supported

### ACTION
Execute a side effect:
- `send_alert` — Create observability alert
- `continue_exam` — Allow exam to proceed
- `pause_exam` — Pause student session
- `disqualify` — Disqualify student
- `send_notification` — Push notification

## Example: Exam Proctoring Workflow

```json
{
  "name": "Exam Proctoring Agent",
  "nodes": [
    { "id": "n1", "type": "TRIGGER", "label": "Exam Start", "config": { "eventType": "exam.start" } },
    { "id": "n2", "type": "REASON", "label": "Analyze Behavior", "config": { "prompt": "Analyze patterns" } },
    { "id": "n3", "type": "TOOL", "label": "Proctor Check", "config": { "toolName": "proctor_check" } },
    { "id": "n4", "type": "DECISION", "label": "Integrity OK?", "config": { "condition": "accuracy > 0.6" } },
    { "id": "n5", "type": "ACTION", "label": "Send Alert", "config": { "actionType": "send_alert" } },
    { "id": "n6", "type": "ACTION", "label": "Continue", "config": { "actionType": "continue_exam" } }
  ],
  "edges": [
    { "id": "e1", "source": "n1", "target": "n2" },
    { "id": "e2", "source": "n2", "target": "n3" },
    { "id": "e3", "source": "n3", "target": "n4" },
    { "id": "e4", "source": "n4", "target": "n5", "condition": "false" },
    { "id": "e5", "source": "n4", "target": "n6", "condition": "true" }
  ]
}
```

## Execution Model

1. Workflow triggered by event or API call
2. Engine finds TRIGGER node
3. Executes nodes using Java 21 virtual threads
4. DECISION nodes branch to true/false paths
5. Parallel fan-out for independent branches
6. Results published to RabbitMQ
7. Execution metrics recorded

## Creating Workflows via API

```bash
curl -X POST http://localhost:8080/api/v1/admin/workflows \
  -H "Content-Type: application/json" \
  -d @workflow.json
```

## Executing Workflows

```bash
curl -X POST http://localhost:8080/api/v1/admin/workflows/wf-exam-proctor/execute \
  -H "Content-Type: application/json" \
  -d '{"studentId": "s1", "subject": "PHYSICS", "accuracy": 0.65}'
```
