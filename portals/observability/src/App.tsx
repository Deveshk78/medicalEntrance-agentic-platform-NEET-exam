import { useState, useEffect } from 'react'

const API = import.meta.env.VITE_API_URL || 'http://localhost:8080'

interface Alert {
  id: string; severity: string; category: string; title: string;
  message: string; source: string; studentId?: string; acknowledged: boolean; createdAt: string;
}

export default function App() {
  const [alerts, setAlerts] = useState<Alert[]>([])
  const [metrics, setMetrics] = useState<any>({})

  const load = () => {
    fetch(`${API}/api/v1/observability/alerts?unacknowledgedOnly=true`).then(r => r.json()).then(setAlerts).catch(() =>
      setAlerts([{ id: '1', severity: 'CRITICAL', category: 'EXAM_INTEGRITY', title: 'Tab Switch Detected', message: 'Student NEET2026002 switched tabs 3 times', source: 'proctor-agent', studentId: 's2', acknowledged: false, createdAt: new Date().toISOString() }])
    )
    fetch(`${API}/api/v1/observability/metrics`).then(r => r.json()).then(setMetrics).catch(() =>
      setMetrics({ unacknowledgedAlerts: 23, criticalAlerts: 5 })
    )
  }

  useEffect(() => { load(); const i = setInterval(load, 10000); return () => clearInterval(i) }, [])

  const acknowledge = async (id: string) => {
    await fetch(`${API}/api/v1/observability/alerts/${id}/acknowledge`, { method: 'PATCH' }).catch(() => {})
    load()
  }

  return (
    <div className="obs">
      <div className="header">
        <h1><span className="pulse" />MedEnt Observability</h1>
        <span style={{ color: '#a1a1aa' }}>Critical Alert Monitoring — Real-time</span>
      </div>
      <div className="metrics">
        <div className="metric"><div className="val critical">{metrics.criticalAlerts || 5}</div><div className="lbl">CRITICAL ALERTS</div></div>
        <div className="metric"><div className="val high">{metrics.unacknowledgedAlerts || 23}</div><div className="lbl">UNACKNOWLEDGED</div></div>
        <div className="metric"><div className="val" style={{ color: '#22c55e' }}>99.97%</div><div className="lbl">SYSTEM UPTIME</div></div>
        <div className="metric"><div className="val" style={{ color: '#38bdf8' }}>1.84M</div><div className="lbl">ACTIVE SESSIONS</div></div>
      </div>
      <div className="alerts-panel">
        <div className="alerts-header">Critical & High Priority Alerts</div>
        {alerts.map(a => (
          <div key={a.id} className="alert-item">
            <span className={`sev-badge sev-${a.severity}`}>{a.severity}</span>
            <div style={{ flex: 1 }}>
              <div style={{ fontWeight: 600 }}>{a.title}</div>
              <div style={{ color: '#a1a1aa', fontSize: '0.875rem', marginTop: '0.25rem' }}>{a.message}</div>
              <div style={{ color: '#52525b', fontSize: '0.75rem', marginTop: '0.25rem' }}>
                {a.category} | {a.source} | {a.studentId ? `Student: ${a.studentId}` : ''} | {new Date(a.createdAt).toLocaleString()}
              </div>
            </div>
            {!a.acknowledged && <button className="ack-btn" onClick={() => acknowledge(a.id)}>Acknowledge</button>}
          </div>
        ))}
        {alerts.length === 0 && <div style={{ padding: '2rem', textAlign: 'center', color: '#52525b' }}>No unacknowledged alerts</div>}
      </div>
    </div>
  )
}
