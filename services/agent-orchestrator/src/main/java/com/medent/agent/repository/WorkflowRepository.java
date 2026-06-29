package com.medent.agent.repository;

import com.medent.agent.model.AgentWorkflow;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WorkflowRepository extends MongoRepository<AgentWorkflow, String> {
    List<AgentWorkflow> findByActiveTrue();
    Optional<AgentWorkflow> findByNameAndVersion(String name, String version);
}
