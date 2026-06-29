from fastapi import APIRouter, Request

router = APIRouter()


@router.get("/dashboard")
async def dashboard(request: Request):
  store = request.app.state.data_store
  stats = await store.get_exam_stats()
  return {
    "overview": stats,
    "real_time": {
      "exams_per_minute": 12_450,
      "peak_concurrent": 1_923_000,
      "system_health": "HEALTHY",
    },
  }


@router.get("/subjects")
async def subject_analytics():
  return {
    "PHYSICS": {
      "total_questions": 45,
      "avg_time_per_question_sec": 72,
      "difficulty_distribution": {"easy": 30, "medium": 50, "hard": 20},
      "top_weak_topics": ["Thermodynamics", "Optics", "Electromagnetism"],
    },
    "CHEMISTRY": {
      "total_questions": 45,
      "avg_time_per_question_sec": 68,
      "difficulty_distribution": {"easy": 35, "medium": 45, "hard": 20},
      "top_weak_topics": ["Organic Chemistry", "Coordination Compounds", "Electrochemistry"],
    },
    "BOTANY": {
      "total_questions": 45,
      "avg_time_per_question_sec": 55,
      "difficulty_distribution": {"easy": 40, "medium": 40, "hard": 20},
      "top_weak_topics": ["Plant Physiology", "Ecology", "Morphology"],
    },
    "ZOOLOGY": {
      "total_questions": 45,
      "avg_time_per_question_sec": 58,
      "difficulty_distribution": {"easy": 38, "medium": 42, "hard": 20},
      "top_weak_topics": ["Human Physiology", "Genetics", "Evolution"],
    },
  }


@router.get("/geographic")
async def geographic_distribution():
  return {
    "regions": [
      {"state": "Uttar Pradesh", "students": 425_000, "avg_score": 57.2},
      {"state": "Maharashtra", "students": 312_000, "avg_score": 61.5},
      {"state": "Bihar", "students": 298_000, "avg_score": 55.8},
      {"state": "Rajasthan", "students": 245_000, "avg_score": 59.1},
      {"state": "Karnataka", "students": 198_000, "avg_score": 63.4},
    ]
  }


@router.get("/time-series")
async def time_series():
  return {
    "hourly_active": [1_200_000, 1_450_000, 1_780_000, 1_920_000, 1_847_000],
    "hourly_submissions": [0, 12_000, 45_000, 128_000, 312_000],
    "labels": ["08:00", "10:00", "12:00", "14:00", "16:00"],
  }
