package com.medent.agent.controller;

import com.medent.agent.model.Alert;
import com.medent.agent.repository.AlertRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/observability")
public class ObservabilityController {

    private final AlertRepository alertRepository;

    public ObservabilityController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping("/alerts")
    public List<Alert> getAlerts(@RequestParam(defaultValue = "false") boolean unacknowledgedOnly) {
        return unacknowledgedOnly
                ? alertRepository.findByAcknowledgedFalseOrderByCreatedAtDesc()
                : alertRepository.findAll();
    }

    @GetMapping("/alerts/critical")
    public List<Alert> getCriticalAlerts() {
        return alertRepository.findBySeverityOrderByCreatedAtDesc(Alert.AlertSeverity.CRITICAL);
    }

    @PostMapping("/alerts")
    public Alert createAlert(@RequestBody Map<String, String> body) {
        var alert = new Alert(
                UUID.randomUUID().toString(),
                Alert.AlertSeverity.valueOf(body.getOrDefault("severity", "MEDIUM")),
                Alert.AlertCategory.valueOf(body.getOrDefault("category", "SYSTEM")),
                body.get("title"),
                body.get("message"),
                body.getOrDefault("source", "system"),
                body.get("studentId"),
                false,
                Instant.now()
        );
        return alertRepository.save(alert);
    }

    @PatchMapping("/alerts/{id}/acknowledge")
    public Alert acknowledge(@PathVariable String id) {
        var alert = alertRepository.findById(id).orElseThrow();
        var updated = new Alert(
                alert.id(), alert.severity(), alert.category(), alert.title(),
                alert.message(), alert.source(), alert.studentId(), true, alert.createdAt()
        );
        return alertRepository.save(updated);
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return Map.of(
                "unacknowledgedAlerts", alertRepository.countByAcknowledgedFalse(),
                "criticalAlerts", alertRepository.findBySeverityOrderByCreatedAtDesc(Alert.AlertSeverity.CRITICAL).size(),
                "timestamp", Instant.now().toString()
        );
    }
}
