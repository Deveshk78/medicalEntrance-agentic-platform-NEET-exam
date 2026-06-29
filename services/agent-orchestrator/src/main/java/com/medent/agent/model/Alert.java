package com.medent.agent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "alerts")
public record Alert(
        @Id String id,
        AlertSeverity severity,
        AlertCategory category,
        String title,
        String message,
        String source,
        String studentId,
        boolean acknowledged,
        Instant createdAt
) {
    public enum AlertSeverity { CRITICAL, HIGH, MEDIUM, LOW, INFO }
    public enum AlertCategory {
        EXAM_INTEGRITY, PERFORMANCE, SYSTEM, SECURITY, AGENT_FAILURE, CAPACITY
    }
}
