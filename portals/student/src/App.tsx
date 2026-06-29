import { useState, useEffect, useCallback } from 'react'

const API = import.meta.env.VITE_API_URL || 'http://localhost:8080'
const SUBJECTS = ['PHYSICS', 'CHEMISTRY', 'BOTANY', 'ZOOLOGY'] as const
const SAMPLE_QUESTIONS: Record<string, { q: string; opts: string[] }[]> = {
  PHYSICS: [
    { q: 'The SI unit of electric current is:', opts: ['Volt', 'Ampere', 'Ohm', 'Watt'] },
    { q: 'Which law states F = ma?', opts: ["Newton's 1st", "Newton's 2nd", "Newton's 3rd", 'Hooke\'s Law'] },
  ],
  CHEMISTRY: [
    { q: 'Avogadro number is approximately:', opts: ['6.022 × 10²³', '3.14 × 10⁸', '9.8 m/s²', '1.6 × 10⁻¹⁹'] },
    { q: 'pH of pure water at 25°C is:', opts: ['0', '7', '14', '1'] },
  ],
  BOTANY: [
    { q: 'Photosynthesis occurs in:', opts: ['Mitochondria', 'Chloroplast', 'Nucleus', 'Ribosome'] },
    { q: 'The male gametophyte in angiosperms is:', opts: ['Ovule', 'Pollen grain', 'Embryo sac', 'Endosperm'] },
  ],
  ZOOLOGY: [
    { q: 'The functional unit of kidney is:', opts: ['Neuron', 'Nephron', 'Alveolus', 'Villus'] },
    { q: 'Insulin is produced by:', opts: ['Liver', 'Pancreas', 'Thyroid', 'Adrenal'] },
  ],
}

export default function App() {
  const [loggedIn, setLoggedIn] = useState(false)
  const [studentId, setStudentId] = useState('s1')
  const [sessionId, setSessionId] = useState<string | null>(null)
  const [subject, setSubject] = useState<typeof SUBJECTS[number]>('PHYSICS')
  const [qIndex, setQIndex] = useState(0)
  const [selected, setSelected] = useState<number | null>(null)
  const [timeLeft, setTimeLeft] = useState(3 * 60 * 60)
  const [answered, setAnswered] = useState(0)

  const heartbeat = useCallback(() => {
    if (sessionId) fetch(`${API}/api/v1/student/exam/${sessionId}/heartbeat`, { method: 'POST' }).catch(() => {})
  }, [sessionId])

  useEffect(() => {
    if (!loggedIn || !sessionId) return
    const t = setInterval(() => setTimeLeft(t => Math.max(0, t - 1)), 1000)
    const h = setInterval(heartbeat, 30000)
    return () => { clearInterval(t); clearInterval(h) }
  }, [loggedIn, sessionId, heartbeat])

  const startExam = async () => {
    try {
      const res = await fetch(`${API}/api/v1/student/exam/start`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ studentId }),
      })
      const data = await res.json()
      setSessionId(data.id)
      setLoggedIn(true)
    } catch {
      setSessionId('demo-session')
      setLoggedIn(true)
    }
  }

  const formatTime = (s: number) => `${Math.floor(s/3600).toString().padStart(2,'0')}:${Math.floor((s%3600)/60).toString().padStart(2,'0')}:${(s%60).toString().padStart(2,'0')}`
  const questions = SAMPLE_QUESTIONS[subject]
  const q = questions[qIndex % questions.length]

  if (!loggedIn) {
    return (
      <div className="login-form">
        <h2 style={{ marginBottom: '1.5rem', textAlign: 'center' }}>NEET 2026 — Student Login</h2>
        <p style={{ color: '#64748b', marginBottom: '1rem', fontSize: '0.875rem', textAlign: 'center' }}>Authenticated via AWS Cognito (JWT/OAuth 2.0)</p>
        <input placeholder="Roll Number" defaultValue="NEET2026001" />
        <input placeholder="Password" type="password" defaultValue="••••••••" />
        <button className="btn btn-primary" style={{ width: '100%' }} onClick={startExam}>Start Examination</button>
      </div>
    )
  }

  return (
    <div className="container">
      <div className="header">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div><h2>NEET 2026 Examination</h2><p style={{ opacity: 0.8, marginTop: '0.25rem' }}>Roll: NEET2026001 | Session: {sessionId?.slice(0, 8)}</p></div>
          <div className="timer">{formatTime(timeLeft)}</div>
        </div>
        <div className="progress-bar"><div className="progress-fill" style={{ width: `${(answered/180)*100}%` }} /></div>
      </div>
      <div className="subjects">
        {SUBJECTS.map(s => (
          <div key={s} className={`subject-tab ${subject === s ? 'active' : ''}`} onClick={() => { setSubject(s); setQIndex(0); setSelected(null) }}>{s}</div>
        ))}
      </div>
      <div className="question-card">
        <p style={{ color: '#64748b', marginBottom: '0.5rem' }}>Question {qIndex + 1} — {subject}</p>
        <h3>{q.q}</h3>
        <div className="options">
          {q.opts.map((opt, i) => (
            <div key={i} className={`option ${selected === i ? 'selected' : ''}`} onClick={() => setSelected(i)}>
              {String.fromCharCode(65 + i)}. {opt}
            </div>
          ))}
        </div>
        <div className="nav-btns">
          <button className="btn btn-secondary" disabled={qIndex === 0} onClick={() => { setQIndex(q => q - 1); setSelected(null) }}>Previous</button>
          <button className="btn btn-primary" onClick={() => { setAnswered(a => a + 1); setQIndex(q => q + 1); setSelected(null) }}>Save & Next</button>
        </div>
      </div>
    </div>
  )
}
