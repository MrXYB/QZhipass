package org.microsoft.qintelipass.controllers;

import lombok.extern.slf4j.Slf4j;
import org.microsoft.qintelipass.dtos.request.CreateAgentRequest;
import org.microsoft.qintelipass.dtos.response.AgentResponse;
import org.microsoft.qintelipass.security.SecurityUtil;
import org.microsoft.qintelipass.services.agent.AgentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/v1/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * 创建Agent
     * POST /api/v1/agents
     */
    @PostMapping
    public ResponseEntity<?> createAgent(@RequestBody CreateAgentRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        SecurityUtil.requireAuthentication();

        try {
            AgentResponse agent = agentService.createAgent(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "data", agent,
                    "message", "Agent创建成功"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取Agent调用列表（公共模板 + 用户自定义）
     * GET /api/v1/agents
     */
    @GetMapping
    public ResponseEntity<?> listAgents() {
        Long userId = SecurityUtil.getCurrentUserId();
        SecurityUtil.requireAuthentication();

        List<AgentResponse> agents = agentService.listAgents(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", agents
        ));
    }

    /**
     * 删除用户自定义Agent
     * DELETE /api/v1/agents/{agentId}
     */
    @DeleteMapping("/{agentId}")
    public ResponseEntity<?> deleteAgent(@PathVariable Long agentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        SecurityUtil.requireAuthentication();

        try {
            agentService.deleteAgent(userId, agentId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Agent已删除"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
