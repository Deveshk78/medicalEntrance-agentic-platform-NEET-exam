package com.medent.agent.service;

import com.medent.agent.model.*;
import com.medent.agent.repository.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ExamService {

    private final ExamSessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final AlertRepository alertRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    public ExamService(ExamSessionRepository sessionRepository,
                       StudentRepository studentRepository,
                       AlertRepository alertRepository,
                       RedisTemplate<String, Object> redisTemplate,
                       RabbitTemplate rabbitTemplate) {
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
        this.alertRepository = alertRepository;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    public ExamSession startExam(String studentId, String ipAddress) {
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));

        var progress = new HashMap<String, ExamSession.SubjectProgress>();
        for (var subject : student.subjects()) {
            progress.put(subject.name(), new ExamSession.SubjectProgress(subject.name(), 45, 0, 0, 0.0));
        }

        var session = new ExamSession(
                UUID.randomUUID().toString(),
                studentId,
                student.fullName(),
                student.rollNumber(),
                ExamSession.ExamSessionStatus.ACTIVE,
                progress,
                180,
                0,
                0,
                Instant.now(),
                Instant.now(),
                ipAddress
        );

        sessionRepository.save(session);
        redisTemplate.opsForValue().set("session:" + session.id(), session, 4, TimeUnit.HOURS);
        rabbitTemplate.convertAndSend("medent.agent.events", "exam.started", Map.of(
                "sessionId", session.id(), "studentId", studentId
        ));
        return session;
    }

    public ExamSession heartbeat(String sessionId) {
        var session = getSession(sessionId);
        var updated = new ExamSession(
                session.id(), session.studentId(), session.studentName(), session.rollNumber(),
                session.status(), session.subjectProgress(), session.totalQuestions(),
                session.answeredQuestions(), session.correctAnswers(),
                session.startedAt(), Instant.now(), session.ipAddress()
        );
        sessionRepository.save(updated);
        redisTemplate.opsForValue().set("session:" + sessionId, updated, 4, TimeUnit.HOURS);
        return updated;
    }

    public List<ExamSession> getActiveSessions() {
        return sessionRepository.findByStatus(ExamSession.ExamSessionStatus.ACTIVE);
    }

    public ExamSession getSession(String sessionId) {
        var cached = redisTemplate.opsForValue().get("session:" + sessionId);
        if (cached instanceof ExamSession s) return s;
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found"));
    }

    public Map<String, Object> getDashboardStats() {
        long activeStudents = sessionRepository.countByStatus(ExamSession.ExamSessionStatus.ACTIVE);
        long totalRegistered = studentRepository.count();
        long criticalAlerts = alertRepository.countByAcknowledgedFalse();

        return Map.of(
                "activeStudents", activeStudents,
                "totalRegistered", totalRegistered,
                "criticalAlerts", criticalAlerts,
                "capacity", 2_500_000,
                "utilizationPercent", (double) activeStudents / 2_500_000 * 100,
                "subjects", List.of("PHYSICS", "CHEMISTRY", "BOTANY", "ZOOLOGY"),
                "timestamp", Instant.now().toString()
        );
    }
}
