package com.medent.agent.agent;

import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of agent tools and DJL-based inference for subject classification.
 */
@Component
public class AgentToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(AgentToolRegistry.class);
    private final Map<String, AgentTool> tools = new ConcurrentHashMap<>();

    public AgentToolRegistry() {
        registerDefaultTools();
    }

    private void registerDefaultTools() {
        tools.put("fetch_student_profile", ctx -> Map.of("tool", "fetch_student_profile", "status", "ok"));
        tools.put("check_exam_integrity", ctx -> Map.of("tool", "check_exam_integrity", "violations", 0));
        tools.put("calculate_score", ctx -> {
            int correct = ((Number) ctx.getOrDefault("correct", 0)).intValue();
            int total = ((Number) ctx.getOrDefault("total", 1)).intValue();
            return Map.of("score", (double) correct / total * 100, "correct", correct, "total", total);
        });
        tools.put("send_alert", ctx -> Map.of("tool", "send_alert", "sent", true, "severity", ctx.getOrDefault("severity", "MEDIUM")));
        tools.put("recommend_study_plan", ctx -> Map.of(
                "tool", "recommend_study_plan",
                "weakSubjects", java.util.List.of("PHYSICS", "CHEMISTRY"),
                "hoursPerDay", 4
        ));
        tools.put("proctor_check", ctx -> Map.of("tool", "proctor_check", "faceDetected", true, "tabSwitch", false));
        tools.put("time_warning", ctx -> Map.of("tool", "time_warning", "minutesLeft", ctx.getOrDefault("timeRemaining", 30)));
    }

    public Map<String, Object> invoke(String toolName, Map<String, Object> params) {
        var tool = tools.get(toolName);
        if (tool == null) {
            log.warn("Unknown tool: {}", toolName);
            return Map.of("error", "Unknown tool: " + toolName);
        }
        return tool.execute(params != null ? params : Map.of());
    }

    /**
     * DJL-based subject confidence scoring using a lightweight linear model.
     */
    public double classifySubject(String subject, Map<String, Object> context) {
        try (NDManager manager = NDManager.newBaseManager()) {
            double[] features = extractFeatures(subject, context);
            NDArray input = manager.create(features).reshape(new Shape(1, features.length));

            // Simple weighted scoring — production would load a trained DJL model
            double score = 0.0;
            for (int i = 0; i < features.length; i++) {
                score += features[i] * (0.25 + i * 0.1);
            }
            return Math.min(1.0, Math.max(0.0, score / features.length));
        } catch (Exception e) {
            log.warn("DJL classification fallback: {}", e.getMessage());
            return 0.75;
        }
    }

    private double[] extractFeatures(String subject, Map<String, Object> context) {
        return new double[]{
                ((Number) context.getOrDefault("accuracy", 0.5)).doubleValue(),
                ((Number) context.getOrDefault("timeSpent", 0)).doubleValue() / 3600.0,
                ((Number) context.getOrDefault("questionsAnswered", 0)).doubleValue() / 180.0,
                subjectHash(subject)
        };
    }

    private double subjectHash(String subject) {
        return switch (subject.toUpperCase()) {
            case "PHYSICS" -> 0.25;
            case "CHEMISTRY" -> 0.50;
            case "BOTANY" -> 0.75;
            case "ZOOLOGY" -> 1.0;
            default -> 0.5;
        };
    }

    @FunctionalInterface
    public interface AgentTool {
        Map<String, Object> execute(Map<String, Object> context);
    }
}
