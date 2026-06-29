package com.medent.agent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "exam_sessions")
public record ExamSession(
        @Id String id,
        String studentId,
        String studentName,
        String rollNumber,
        ExamSessionStatus status,
        Map<String, SubjectProgress> subjectProgress,
        int totalQuestions,
        int answeredQuestions,
        int correctAnswers,
        Instant startedAt,
        Instant lastHeartbeat,
        String ipAddress
) {
    public enum ExamSessionStatus { ACTIVE, PAUSED, SUBMITTED, TIMED_OUT }

    public record SubjectProgress(
            String subject,
            int total,
            int answered,
            int correct,
            double accuracy
    ) {}
}
