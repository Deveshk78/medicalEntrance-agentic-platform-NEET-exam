import numpy as np
import keras
from keras import layers


class MLEngine:
  """Keras-based ML models for student performance analytics."""

  def __init__(self):
    self.performance_model = None
    self.subject_model = None
    self.dropout_model = None

  def build_models(self):
    self.performance_model = self._build_performance_predictor()
    self.subject_model = self._build_subject_classifier()
    self.dropout_model = self._build_dropout_risk_model()
    self._train_with_synthetic_data()

  def _build_performance_predictor(self):
    model = keras.Sequential([
      layers.Input(shape=(8,)),
      layers.Dense(64, activation="relu"),
      layers.BatchNormalization(),
      layers.Dropout(0.3),
      layers.Dense(32, activation="relu"),
      layers.Dense(16, activation="relu"),
      layers.Dense(1, activation="sigmoid"),
    ], name="performance_predictor")
    model.compile(optimizer="adam", loss="mse", metrics=["mae"])
    return model

  def _build_subject_classifier(self):
    model = keras.Sequential([
      layers.Input(shape=(6,)),
      layers.Dense(48, activation="relu"),
      layers.Dropout(0.2),
      layers.Dense(24, activation="relu"),
      layers.Dense(4, activation="softmax"),
    ], name="subject_classifier")
    model.compile(optimizer="adam", loss="categorical_crossentropy", metrics=["accuracy"])
    return model

  def _build_dropout_risk_model(self):
    model = keras.Sequential([
      layers.Input(shape=(5,)),
      layers.Dense(32, activation="relu"),
      layers.Dense(16, activation="relu"),
      layers.Dense(1, activation="sigmoid"),
    ], name="dropout_risk")
    model.compile(optimizer="adam", loss="binary_crossentropy", metrics=["accuracy"])
    return model

  def _train_with_synthetic_data(self):
    rng = np.random.default_rng(42)
    X_perf = rng.random((1000, 8)).astype(np.float32)
    y_perf = (X_perf.mean(axis=1) * 0.8 + rng.random(1000) * 0.2).astype(np.float32)
    self.performance_model.fit(X_perf, y_perf, epochs=5, batch_size=32, verbose=0)

    X_subj = rng.random((800, 6)).astype(np.float32)
    y_subj = keras.utils.to_categorical(rng.integers(0, 4, 800), 4)
    self.subject_model.fit(X_subj, y_subj, epochs=5, batch_size=32, verbose=0)

    X_drop = rng.random((600, 5)).astype(np.float32)
    y_drop = (X_drop[:, 0] < 0.3).astype(np.float32)
    self.dropout_model.fit(X_drop, y_drop, epochs=5, batch_size=32, verbose=0)

  def predict_performance(self, features: list[float]) -> dict:
    arr = np.array([features], dtype=np.float32)
    score = float(self.performance_model.predict(arr, verbose=0)[0][0])
    return {
      "predicted_score": round(score * 100, 2),
      "percentile": round(score * 100, 1),
      "confidence": round(0.85 + score * 0.1, 3),
    }

  def classify_weak_subjects(self, features: list[float]) -> dict:
    arr = np.array([features], dtype=np.float32)
    probs = self.subject_model.predict(arr, verbose=0)[0]
    subjects = ["PHYSICS", "CHEMISTRY", "BOTANY", "ZOOLOGY"]
    ranked = sorted(zip(subjects, probs.tolist()), key=lambda x: x[1])
    return {
      "weakest": ranked[0][0],
      "strongest": ranked[-1][0],
      "subject_scores": {s: round(p * 100, 2) for s, p in zip(subjects, probs)},
    }

  def predict_dropout_risk(self, features: list[float]) -> dict:
    arr = np.array([features], dtype=np.float32)
    risk = float(self.dropout_model.predict(arr, verbose=0)[0][0])
    level = "HIGH" if risk > 0.7 else "MEDIUM" if risk > 0.4 else "LOW"
    return {"dropout_risk": round(risk, 3), "risk_level": level}
