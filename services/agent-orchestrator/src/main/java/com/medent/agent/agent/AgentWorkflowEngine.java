package com.medent.agent.agent;

import com.medent.agent.model.AgentWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core agent workflow engine using Java 21 virtual threads and structured concurrency.
 * Executes low-code/no-code drag-and-drop workflows: reason → tool → decide → act.
 */
@Service
public class AgentWorkflowEngine {

    private static final Logger log = LoggerFactory.getLogger(AgentWorkflowEngine.class);
    private static final String EXCHANGE = "medent.agent.events";

    private final RabbitTemplate rabbitTemplate;
    private final AgentToolRegistry toolRegistry;
    private final Map<String, AtomicInteger> executionCounters = new ConcurrentHashMap<>();

    public AgentWorkflowEngine(RabbitTemplate rabbitTemplate, AgentToolRegistry toolRegistry) {
        this.rabbitTemplate = rabbitTemplate;
        this.toolRegistry = toolRegistry;
    }

    public AgentExecutionResult execute(AgentWorkflow workflow, Map<String, Object> context) {
        String executionId = UUID.randomUUID().toString();
        log.info("Starting workflow execution {} for workflow {}", executionId, workflow.name());

        var nodeMap = buildNodeMap(workflow);
        var adjacency = buildAdjacency(workflow);
        var results = new ConcurrentHashMap<String, Object>();
        var startNode = findStartNode(workflow);

        if (startNode == null) {
            return AgentExecutionResult.failure(executionId, "No trigger node found in workflow");
        }

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            scope.fork(() -> {
                traverse(startNode.id(), nodeMap, adjacency, context, results);
                return null;
            });
            scope.join();
            scope.throwIfFailed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return AgentExecutionResult.failure(executionId, "Execution interrupted");
        } catch (Exception e) {
            log.error("Workflow execution failed", e);
            return AgentExecutionResult.failure(executionId, e.getMessage());
        }

        publishEvent("workflow.completed", Map.of(
                "executionId", executionId,
                "workflowId", workflow.id(),
                "results", results,
                "timestamp", Instant.now().toString()
        ));

        executionCounters.computeIfAbsent(workflow.id(), k -> new AtomicInteger()).incrementAndGet();
        return AgentExecutionResult.success(executionId, Map.copyOf(results));
    }

    private void traverse(String nodeId, Map<String, AgentNode> nodeMap,
                          Map<String, List<String>> adjacency,
                          Map<String, Object> context,
                          ConcurrentHashMap<String, Object> results) {
        var node = nodeMap.get(nodeId);
        if (node == null) return;

        Object output = switch (node) {
            case TriggerNode t -> executeTrigger(t, context);
            case ReasoningNode r -> executeReasoning(r, context);
            case ToolCallNode t -> executeToolCall(t, context);
            case DecisionNode d -> executeDecision(d, context);
            case ActionNode a -> executeAction(a, context);
        };

        results.put(nodeId, output);

        var nextNodes = adjacency.getOrDefault(nodeId, List.of());
        if (node instanceof DecisionNode d) {
            String branch = (String) output;
            nextNodes = nextNodes.stream()
                    .filter(n -> matchesCondition(nodeMap.get(n), branch))
                    .toList();
        }

        // Parallel fan-out using virtual threads
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (String next : nextNodes) {
                scope.fork(() -> {
                    traverse(next, nodeMap, adjacency, context, results);
                    return null;
                });
            }
            scope.join();
            scope.throwIfFailed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Node traversal failed at {}", nodeId, e);
        }
    }

    private Object executeTrigger(TriggerNode node, Map<String, Object> context) {
        log.debug("Trigger: {} event={}", node.id(), node.eventType());
        return Map.of("triggered", true, "event", node.eventType());
    }

    private Object executeReasoning(ReasoningNode node, Map<String, Object> context) {
        String prompt = node.prompt() != null ? node.prompt() : (String) node.config().get("prompt");
        log.debug("Reasoning: {} prompt={}", node.id(), prompt);

        // DJL-based lightweight inference for subject classification / reasoning
        String subject = (String) context.getOrDefault("subject", "PHYSICS");
        double confidence = toolRegistry.classifySubject(subject, context);
        return Map.of("reasoning", prompt, "confidence", confidence, "subject", subject);
    }

    private Object executeToolCall(ToolCallNode node, Map<String, Object> context) {
        log.debug("Tool call: {} tool={}", node.id(), node.toolName());
        return toolRegistry.invoke(node.toolName(), node.params() != null ? node.params() : node.config());
    }

    private Object executeDecision(DecisionNode node, Map<String, Object> context) {
        String condition = node.condition() != null ? node.condition() : (String) node.config().get("condition");
        boolean result = evaluateCondition(condition, context);
        log.debug("Decision: {} condition={} result={}", node.id(), condition, result);
        return result ? "true" : "false";
    }

    private Object executeAction(ActionNode node, Map<String, Object> context) {
        log.debug("Action: {} type={}", node.id(), node.actionType());
        publishEvent("agent.action", Map.of(
                "nodeId", node.id(),
                "actionType", node.actionType(),
                "payload", node.payload() != null ? node.payload() : node.config(),
                "context", context
        ));
        return Map.of("action", node.actionType(), "status", "executed");
    }

    private boolean evaluateCondition(String condition, Map<String, Object> context) {
        if (condition == null) return true;
        if (condition.contains("accuracy")) {
            double accuracy = ((Number) context.getOrDefault("accuracy", 0.0)).doubleValue();
            return accuracy >= 0.6;
        }
        if (condition.contains("time_remaining")) {
            int remaining = ((Number) context.getOrDefault("timeRemaining", 0)).intValue();
            return remaining < 300;
        }
        return true;
    }

    private boolean matchesCondition(AgentNode node, String branch) {
        if (node == null) return true;
        if (node.config() != null && node.config().containsKey("branch")) {
            return branch.equals(node.config().get("branch"));
        }
        return true;
    }

    private Map<String, AgentNode> buildNodeMap(AgentWorkflow workflow) {
        var map = new HashMap<String, AgentNode>();
        for (var n : workflow.nodes()) {
            map.put(n.id(), toAgentNode(n));
        }
        return map;
    }

    private AgentNode toAgentNode(AgentWorkflow.WorkflowNode n) {
        return switch (n.type().toUpperCase()) {
            case "TRIGGER" -> new TriggerNode(n.id(), n.type(), n.config(), (String) n.config().getOrDefault("eventType", "exam.start"));
            case "REASON" -> new ReasoningNode(n.id(), n.type(), n.config(), (String) n.config().get("prompt"));
            case "TOOL" -> new ToolCallNode(n.id(), n.type(), n.config(), (String) n.config().get("toolName"), n.config());
            case "DECISION" -> new DecisionNode(n.id(), n.type(), n.config(), (String) n.config().get("condition"));
            case "ACTION" -> new ActionNode(n.id(), n.type(), n.config(), (String) n.config().get("actionType"), n.config());
            default -> new TriggerNode(n.id(), n.type(), n.config(), "unknown");
        };
    }

    private Map<String, List<String>> buildAdjacency(AgentWorkflow workflow) {
        var adj = new HashMap<String, List<String>>();
        for (var edge : workflow.edges()) {
            adj.computeIfAbsent(edge.source(), k -> new ArrayList<>()).add(edge.target());
        }
        return adj;
    }

    private AgentWorkflow.WorkflowNode findStartNode(AgentWorkflow workflow) {
        return workflow.nodes().stream()
                .filter(n -> "TRIGGER".equalsIgnoreCase(n.type()))
                .findFirst()
                .orElse(workflow.nodes().isEmpty() ? null : workflow.nodes().get(0));
    }

    private void publishEvent(String routingKey, Map<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload);
        } catch (Exception e) {
            log.warn("Failed to publish event {}: {}", routingKey, e.getMessage());
        }
    }

    public record AgentExecutionResult(boolean success, String executionId, Map<String, Object> results, String error) {
        public static AgentExecutionResult success(String id, Map<String, Object> results) {
            return new AgentExecutionResult(true, id, results, null);
        }
        public static AgentExecutionResult failure(String id, String error) {
            return new AgentExecutionResult(false, id, Map.of(), error);
        }
    }
}
