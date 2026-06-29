package com.medent.agent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "students")
public record Student(
        @Id String id,
        String cognitoSub,
        String email,
        String fullName,
        String rollNumber,
        StudentStatus status,
        List<Subject> subjects,
        Instant registeredAt,
        Instant lastActiveAt
) {
    public enum StudentStatus { REGISTERED, EXAM_IN_PROGRESS, EXAM_COMPLETED, DISQUALIFIED }
    public enum Subject { PHYSICS, CHEMISTRY, BOTANY, ZOOLOGY }
}
