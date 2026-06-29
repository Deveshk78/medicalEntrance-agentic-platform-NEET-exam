package com.medent.agent.repository;

import com.medent.agent.model.Alert;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AlertRepository extends MongoRepository<Alert, String> {
    List<Alert> findByAcknowledgedFalseOrderByCreatedAtDesc();
    List<Alert> findBySeverityOrderByCreatedAtDesc(Alert.AlertSeverity severity);
    long countByAcknowledgedFalseAndSeverity(Alert.AlertSeverity severity, Alert.AlertSeverity severity2);
    long countByAcknowledgedFalse();
}
