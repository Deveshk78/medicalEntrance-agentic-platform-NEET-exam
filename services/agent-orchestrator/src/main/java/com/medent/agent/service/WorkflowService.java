package com.medent.agent.service;

import com.medent.agent.agent.AgentWorkflowEngine;
import com.medent.agent.model.AgentWorkflow;
import com.medent.agent.repository.WorkflowRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final AgentWorkflowEngine engine;

    public WorkflowService(WorkflowRepository workflowRepository, AgentWorkflowEngine engine) {
        this.workflowRepository = workflowRepository;
        this.engine = engine;
    }

    public List<AgentWorkflow> listActive() {
        return workflowRepository.findByActiveTrue();
    }

    public AgentWorkflow save(AgentWorkflow workflow) {
        var saved = new AgentWorkflow(
                workflow.id() != null ? workflow.id() : UUID.randomUUID().toString(),
                workflow.name(),
                workflow.description(),
                workflow.version() != null ? workflow.version() : "1.0.0",
                workflow.active(),
                workflow.nodes(),
                workflow.edges(),
                workflow.metadata(),
                workflow.createdAt() != null ? workflow.createdAt() : Instant.now(),
                Instant.now()
        );
        return workflowRepository.save(saved);
    }

    public AgentWorkflowEngine.AgentExecutionResult execute(String workflowId, Map<String, Object> context) {
        var workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Workflow not found"));
        return engine.execute(workflow, context);
    }
}
