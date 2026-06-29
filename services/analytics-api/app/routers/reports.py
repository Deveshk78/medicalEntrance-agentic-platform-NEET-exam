from fastapi import APIRouter

router = APIRouter()


@router.get("/executive-summary")
async def executive_summary():
  return {
    "title": "NEET 2026 Executive Summary",
    "total_registered": 2_500_000,
    "exam_in_progress": 1_847_293,
    "completed": 652_707,
    "disqualified": 1_247,
    "avg_completion_time_min": 178,
    "system_uptime_percent": 99.97,
    "agent_workflows_executed": 4_523_891,
    "critical_alerts": 23,
    "recommendations": [
      "Increase proctoring agents in UP and Bihar regions",
      "Physics section showing highest dropout in last 30 minutes",
      "Scale analytics API replicas during 14:00-16:00 peak",
    ],
  }


@router.get("/agent-performance")
async def agent_performance():
  return {
    "workflows": [
      {"name": "Exam Proctoring Agent", "executions": 1_847_293, "success_rate": 99.8, "avg_latency_ms": 45},
      {"name": "Study Plan Recommender", "executions": 652_707, "success_rate": 99.5, "avg_latency_ms": 120},
      {"name": "Integrity Checker", "executions": 1_847_293, "success_rate": 99.9, "avg_latency_ms": 32},
      {"name": "Time Warning Agent", "executions": 3_200_000, "success_rate": 100.0, "avg_latency_ms": 15},
    ]
  }
