package com.medent.agent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "workflows")
public record AgentWorkflow(
        @Id String id,
        String name,
        String description,
        String version,
        boolean active,
        List<WorkflowNode> nodes,
        List<WorkflowEdge> edges,
        Map<String, Object> metadata,
        Instant createdAt,
        Instant updatedAt
) {
    public record WorkflowNode(
            String id,
            String type,
            String label,
            Map<String, Object> config,
            double positionX,
            double positionY
    ) {}

    public record WorkflowEdge(
            String id,
            String source,
            String target,
            String condition
    ) {}
}
