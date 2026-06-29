package com.medent.agent.config;

import com.medent.agent.model.*;
import com.medent.agent.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Configuration
public class DataSeeder {

    @Bean
    @Profile("!test")
    CommandLineRunner seedData(StudentRepository studentRepo,
                               WorkflowRepository workflowRepo,
                               ExamSessionRepository sessionRepo,
                               AlertRepository alertRepo) {
        return args -> {
            if (studentRepo.count() > 0) return;

            var students = List.of(
                    new Student("s1", "cog-001", "rahul@medent.in", "Rahul Sharma", "NEET2026001",
                            Student.StudentStatus.EXAM_IN_PROGRESS,
                            List.of(Student.Subject.PHYSICS, Student.Subject.CHEMISTRY, Student.Subject.BOTANY, Student.Subject.ZOOLOGY),
                            Instant.now(), Instant.now()),
                    new Student("s2", "cog-002", "priya@medent.in", "Priya Patel", "NEET2026002",
                            Student.StudentStatus.EXAM_IN_PROGRESS,
                            List.of(Student.Subject.PHYSICS, Student.Subject.CHEMISTRY, Student.Subject.BOTANY, Student.Subject.ZOOLOGY),
                            Instant.now(), Instant.now()),
                    new Student("s3", "cog-003", "amit@medent.in", "Amit Kumar", "NEET2026003",
                            Student.StudentStatus.REGISTERED,
                            List.of(Student.Subject.PHYSICS, Student.Subject.CHEMISTRY, Student.Subject.BOTANY, Student.Subject.ZOOLOGY),
                            Instant.now(), Instant.now())
            );
            studentRepo.saveAll(students);

            var workflow = new AgentWorkflow(
                    "wf-exam-proctor",
                    "Exam Proctoring Agent",
                    "Monitors exam integrity, reasons about student behavior, takes proctoring actions",
                    "1.0.0",
                    true,
                    List.of(
                            new AgentWorkflow.WorkflowNode("n1", "TRIGGER", "Exam Start", Map.of("eventType", "exam.start"), 100, 100),
                            new AgentWorkflow.WorkflowNode("n2", "REASON", "Analyze Behavior", Map.of("prompt", "Analyze student exam behavior patterns"), 300, 100),
                            new AgentWorkflow.WorkflowNode("n3", "TOOL", "Proctor Check", Map.of("toolName", "proctor_check"), 500, 100),
                            new AgentWorkflow.WorkflowNode("n4", "DECISION", "Integrity Check", Map.of("condition", "accuracy > 0.6"), 700, 100),
                            new AgentWorkflow.WorkflowNode("n5", "ACTION", "Send Alert", Map.of("actionType", "send_alert", "severity", "HIGH"), 900, 50),
                            new AgentWorkflow.WorkflowNode("n6", "ACTION", "Continue Exam", Map.of("actionType", "continue_exam"), 900, 150)
                    ),
                    List.of(
                            new AgentWorkflow.WorkflowEdge("e1", "n1", "n2", null),
                            new AgentWorkflow.WorkflowEdge("e2", "n2", "n3", null),
                            new AgentWorkflow.WorkflowEdge("e3", "n3", "n4", null),
                            new AgentWorkflow.WorkflowEdge("e4", "n4", "n5", "false"),
                            new AgentWorkflow.WorkflowEdge("e5", "n4", "n6", "true")
                    ),
                    Map.of("category", "proctoring"),
                    Instant.now(), Instant.now()
            );
            workflowRepo.save(workflow);

            var progress = Map.of(
                    "PHYSICS", new ExamSession.SubjectProgress("PHYSICS", 45, 12, 8, 0.67),
                    "CHEMISTRY", new ExamSession.SubjectProgress("CHEMISTRY", 45, 10, 7, 0.70),
                    "BOTANY", new ExamSession.SubjectProgress("BOTANY", 45, 8, 6, 0.75),
                    "ZOOLOGY", new ExamSession.SubjectProgress("ZOOLOGY", 45, 5, 3, 0.60)
            );
            sessionRepo.save(new ExamSession(
                    "sess-001", "s1", "Rahul Sharma", "NEET2026001",
                    ExamSession.ExamSessionStatus.ACTIVE, progress, 180, 35, 24,
                    Instant.now().minusSeconds(3600), Instant.now(), "10.0.0.1"
            ));

            alertRepo.save(new Alert(
                    "alert-001", Alert.AlertSeverity.CRITICAL, Alert.AlertCategory.EXAM_INTEGRITY,
                    "Tab Switch Detected", "Student NEET2026002 switched browser tabs 3 times",
                    "proctor-agent", "s2", false, Instant.now().minusSeconds(120)
            ));
        };
    }
}
