package com.medent.agent.agent;

import java.util.Map;

/**
 * Sealed hierarchy for agent node types — Java 21 pattern matching enabled.
 */
public sealed interface AgentNode permits
        ReasoningNode, ToolCallNode, DecisionNode, ActionNode, TriggerNode {

    String id();
    String type();
    Map<String, Object> config();
}

record ReasoningNode(String id, String type, Map<String, Object> config, String prompt) implements AgentNode {}
record ToolCallNode(String id, String type, Map<String, Object> config, String toolName, Map<String, Object> params) implements AgentNode {}
record DecisionNode(String id, String type, Map<String, Object> config, String condition) implements AgentNode {}
record ActionNode(String id, String type, Map<String, Object> config, String actionType, Map<String, Object> payload) implements AgentNode {}
record TriggerNode(String id, String type, Map<String, Object> config, String eventType) implements AgentNode {}
