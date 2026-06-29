# Analytics

## Overview

The MedEnt Analytics API provides comprehensive, multi-angle analytics powered by Python, Keras, and TensorFlow neural networks.

## ML Models

### 1. Performance Predictor

**Architecture:** 8 → 64 → 32 → 16 → 1 (sigmoid)

| Layer | Activation | Purpose |
|-------|-----------|---------|
| Dense(64) | ReLU + BatchNorm + Dropout(0.3) | Feature extraction |
| Dense(32) | ReLU | Pattern recognition |
| Dense(16) | ReLU | Score refinement |
| Dense(1) | Sigmoid | Normalized score (0-1) |

**Input Features (8):**
1. Physics accuracy
2. Chemistry accuracy
3. Botany accuracy
4. Zoology accuracy
5. Time spent (normalized)
6. Questions answered (normalized)
7. Mock tests taken (normalized)
8. Overall average accuracy

**Output:** Predicted NEET score (0-100), percentile, confidence

### 2. Subject Classifier

**Architecture:** 6 → 48 → 24 → 4 (softmax)

Classifies student performance across four subjects to identify weakest and strongest areas.

### 3. Dropout Risk Model

**Architecture:** 5 → 32 → 16 → 1 (sigmoid)

Predicts probability of a student abandoning the exam mid-session.

**Risk Levels:**
| Score | Level | Action |
|-------|-------|--------|
| > 0.7 | HIGH | Trigger retention agent, send encouragement |
| 0.4-0.7 | MEDIUM | Monitor closely, time warnings |
| < 0.4 | LOW | Normal monitoring |

## Analytics Dimensions

### Real-Time Dashboard
- Active exam count vs 25L capacity
- Exams per minute throughput
- Peak concurrent sessions
- System health status

### Subject Analytics
Per-subject breakdown for Physics, Chemistry, Botany, Zoology:
- Average time per question
- Difficulty distribution (easy/medium/hard)
- Top weak topics
- Accuracy trends

### Geographic Distribution
State-wise analytics:
- Student count per state
- Average scores by region
- Regional performance comparisons

### Time-Series
- Hourly active student count
- Hourly submission rate
- Peak hour identification

### Agent Performance
- Workflow execution counts
- Success rates per workflow
- Average latency per agent type

### Executive Summary
- Total registered vs in-progress vs completed
- Disqualification count
- System uptime percentage
- Total agent workflow executions
- AI-generated recommendations

## Data Pipeline

```
Exam Events (RabbitMQ)
    → Analytics API Consumer
    → MongoDB (raw data)
    → Redis (cached aggregates, 60s TTL)
    → Keras Models (predictions)
    → Admin Portal (visualization)
```

## Caching Strategy

| Key Pattern | TTL | Purpose |
|-------------|-----|---------|
| `analytics:exam_stats` | 60s | Dashboard overview |
| `session:{id}` | 4h | Active exam session |
| `prediction:{studentId}` | 5m | ML prediction cache |

## Scaling Analytics for 25L Students

- Redis cluster for distributed caching
- MongoDB aggregation pipelines with indexes
- Keras model served via TensorFlow Serving in production
- Horizontal scaling of analytics API pods
- Batch prediction jobs for offline reports
