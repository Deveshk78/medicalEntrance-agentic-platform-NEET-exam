package com.medent.agent.controller;

import com.medent.agent.model.AgentWorkflow;
import com.medent.agent.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public List<AgentWorkflow> list() {
        return workflowService.listActive();
    }

    @PostMapping
    public AgentWorkflow create(@RequestBody AgentWorkflow workflow) {
        return workflowService.save(workflow);
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<?> execute(@PathVariable String id, @RequestBody Map<String, Object> context) {
        var result = workflowService.execute(id, context);
        return result.success()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }
}
