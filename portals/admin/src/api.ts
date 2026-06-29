const API = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const ANALYTICS = import.meta.env.VITE_ANALYTICS_URL || 'http://localhost:8000';

export async function fetchDashboard() {
  const [stats, analytics] = await Promise.all([
    fetch(`${API}/api/v1/admin/dashboard/stats`).then(r => r.json()),
    fetch(`${ANALYTICS}/api/v1/analytics/dashboard`).then(r => r.json()),
  ]);
  return { stats, analytics };
}

export async function fetchActiveSessions() {
  const res = await fetch(`${API}/api/v1/admin/exam/sessions/active`);
  return res.json();
}

export async function fetchWorkflows() {
  const res = await fetch(`${API}/api/v1/admin/workflows`);
  return res.json();
}

export async function fetchSubjectAnalytics() {
  const res = await fetch(`${ANALYTICS}/api/v1/analytics/subjects`);
  return res.json();
}

export async function fetchExecutiveSummary() {
  const res = await fetch(`${ANALYTICS}/api/v1/reports/executive-summary`);
  return res.json();
}
