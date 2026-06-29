from fastapi import APIRouter, Request
from pydantic import BaseModel

router = APIRouter()

SUBJECTS = ["PHYSICS", "CHEMISTRY", "BOTANY", "ZOOLOGY"]


class PredictionRequest(BaseModel):
  student_id: str
  physics_accuracy: float = 0.6
  chemistry_accuracy: float = 0.6
  botany_accuracy: float = 0.6
  zoology_accuracy: float = 0.6
  time_spent_hours: float = 2.0
  questions_answered: int = 100
  mock_tests_taken: int = 5


@router.post("/performance")
async def predict_performance(body: PredictionRequest, request: Request):
  ml: "MLEngine" = request.app.state.ml_engine
  features = [
    body.physics_accuracy, body.chemistry_accuracy,
    body.botany_accuracy, body.zoology_accuracy,
    body.time_spent_hours / 3.0,
    body.questions_answered / 180.0,
    body.mock_tests_taken / 10.0,
    sum([body.physics_accuracy, body.chemistry_accuracy,
         body.botany_accuracy, body.zoology_accuracy]) / 4,
  ]
  result = ml.predict_performance(features)
  return {"student_id": body.student_id, **result}


@router.post("/weak-subjects")
async def weak_subjects(body: PredictionRequest, request: Request):
  ml = request.app.state.ml_engine
  features = [
    body.physics_accuracy, body.chemistry_accuracy,
    body.botany_accuracy, body.zoology_accuracy,
    body.time_spent_hours / 3.0,
    body.questions_answered / 180.0,
  ]
  result = ml.classify_weak_subjects(features)
  return {"student_id": body.student_id, **result}


@router.post("/dropout-risk")
async def dropout_risk(body: PredictionRequest, request: Request):
  ml = request.app.state.ml_engine
  avg_acc = (body.physics_accuracy + body.chemistry_accuracy +
             body.botany_accuracy + body.zoology_accuracy) / 4
  features = [
    avg_acc,
    body.time_spent_hours / 3.0,
    body.questions_answered / 180.0,
    body.mock_tests_taken / 10.0,
    1.0 - avg_acc,
  ]
  result = ml.predict_dropout_risk(features)
  return {"student_id": body.student_id, **result}
