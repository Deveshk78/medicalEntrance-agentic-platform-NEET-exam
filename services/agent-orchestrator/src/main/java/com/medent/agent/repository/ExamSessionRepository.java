package com.medent.agent.repository;

import com.medent.agent.model.ExamSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExamSessionRepository extends MongoRepository<ExamSession, String> {
    List<ExamSession> findByStatus(ExamSession.ExamSessionStatus status);
    long countByStatus(ExamSession.ExamSessionStatus status);
    List<ExamSession> findByStudentId(String studentId);
}
