package com.medent.agent.controller;

import com.medent.agent.model.ExamSession;
import com.medent.agent.service.ExamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/student/exam/start")
    public ExamSession startExam(@RequestBody Map<String, String> body,
                                 @RequestHeader(value = "X-Forwarded-For", defaultValue = "127.0.0.1") String ip) {
        return examService.startExam(body.get("studentId"), ip);
    }

    @PostMapping("/student/exam/{sessionId}/heartbeat")
    public ExamSession heartbeat(@PathVariable String sessionId) {
        return examService.heartbeat(sessionId);
    }

    @GetMapping("/admin/exam/sessions/active")
    public List<ExamSession> activeSessions() {
        return examService.getActiveSessions();
    }

    @GetMapping("/admin/dashboard/stats")
    public Map<String, Object> dashboardStats() {
        return examService.getDashboardStats();
    }

    @GetMapping("/public/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "agent-orchestrator");
    }
}
