package org.microsoft.qintelipass.controllers;

import org.microsoft.qintelipass.ai.token.DashboardData;
import org.microsoft.qintelipass.ai.token.DepartmentUsageData;
import org.microsoft.qintelipass.services.agent.TokenService;
import org.microsoft.qintelipass.ai.token.UserTokenStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Token 配额、用量记录和统计看板接口。
 */
@RestController
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    // ==================== 用户视角 ====================

    /**
     * 获取当前用户当日 Token 限额与使用情况。
     */
    @GetMapping("/api/user/token")
    public ResponseEntity<?> getUserToken(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader
    ) {
        Long userId = parseUserId(userIdHeader);
        if (userId == null) {
            return badRequest("Missing or invalid X-User-Id header");
        }

        UserTokenStatus status = tokenService.getDailyStatus(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", status));
    }

    /**
     * 前端 employee-token-stats 页面使用的当日 Token 数据。
     */
    @GetMapping("/api/v1/user/token/usage")
    public ResponseEntity<?> getUserTokenUsage(
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        if (userId == null) {
            return badRequest("Missing X-User-Id header");
        }

        UserTokenStatus status = tokenService.getDailyStatus(userId);
        String state = status.overQuota()
                ? "over"
                : status.quota() > 0 && (double) status.used() / status.quota() > 0.85
                        ? "warning"
                        : "normal";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("daily_limit", status.quota());
        data.put("used_today", status.used());
        data.put("remaining", status.remaining());
        data.put("status", state);
        data.put("department", status.department());
        data.put("name", status.userName());

        return ResponseEntity.ok(Map.of("success", true, "rawData", data));
    }

    /**
     * 本周每日消耗趋势与汇总。
     */
    @GetMapping("/api/v1/user/token/weekly")
    public ResponseEntity<?> getUserWeeklyTrend(
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        if (userId == null) {
            return badRequest("Missing X-User-Id header");
        }

        Map<String, Object> data = tokenService.getUserWeeklyTrend(userId);
        return ResponseEntity.ok(Map.of("success", true, "rawData", data));
    }

    /**
     * 最近对话 Token 消耗记录。
     */
    @GetMapping("/api/v1/user/token/conversations")
    public ResponseEntity<?> getRecentConversations(
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        if (userId == null) {
            return badRequest("Missing X-User-Id header");
        }

        List<Map<String, Object>> data = tokenService.getRecentConversations(userId);
        return ResponseEntity.ok(Map.of("success", true, "rawData", data));
    }

    // ==================== 管理员视角 ====================

    /**
     * 原始管理员可视化看板数据。
     */
    @GetMapping("/api/admin/token/dashboard")
    public ResponseEntity<?> getDashboard() {
        DashboardData data = tokenService.getDashboard();
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    /**
     * 按部门统计的当日 Token 使用表。
     */
    @GetMapping("/api/admin/token/usage")
    public ResponseEntity<?> getDepartmentUsage() {
        DepartmentUsageData data = tokenService.getDepartmentUsage();
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    /**
     * 统一设置所有用户的 Token 限额。
     */
    @PostMapping("/api/admin/token/quota")
    public ResponseEntity<?> setQuota(@RequestBody Map<String, Object> body) {
        Long quota = parseLong(body.get("quota"));
        if (quota == null) {
            return badRequest("quota is required and must be a number");
        }

        try {
            tokenService.setGlobalQuota(quota);
        } catch (IllegalArgumentException exception) {
            return badRequest(exception.getMessage());
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Quota updated",
                "quota", quota
        ));
    }

    // ==================== 管理员前端适配 ====================

    /**
     * admin-token-dashboard 页面使用的 KPI、图表和员工列表。
     *
     * <p>原代码同时把此方法映射到了 /api/admin/token/dashboard，
     * 与 getDashboard() 冲突，会导致 Spring Boot 启动失败。这里保留两套
     * 数据格式，但让它们使用各自唯一的 URL。</p>
     */
    @GetMapping("/api/v1/admin/token/dashboard")
    public ResponseEntity<?> getDashboardForFrontend() {
        try {
            Map<String, Object> rawData = tokenService.getDashboardForFrontend();
            return ResponseEntity.ok(Map.of("success", true, "rawData", rawData));
        } catch (RuntimeException exception) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", safeMessage(exception)));
        }
    }

    /**
     * admin-token-dashboard 页面使用的员工和部门用量。
     */
    @GetMapping("/api/v1/admin/token/usage")
    public ResponseEntity<?> getUsageForFrontend() {
        try {
            DepartmentUsageData data = tokenService.getDepartmentUsage();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("userUsageRows", data.users());
            result.put("departmentRows", data.departments());
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (RuntimeException exception) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", safeMessage(exception)));
        }
    }

    /**
     * 使用前端字段名 daily_token_limit 更新全局限额。
     */
    @PutMapping("/api/v1/admin/token/quota")
    public ResponseEntity<?> setQuotaForFrontend(@RequestBody Map<String, Object> body) {
        Long newLimit = parseLong(body.get("daily_token_limit"));
        if (newLimit == null) {
            return badRequest("daily_token_limit is required and must be a number");
        }
        if (newLimit < 1_000) {
            return badRequest("Token limit must be >= 1000");
        }

        tokenService.setGlobalQuota(newLimit);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("daily_limit", newLimit);
        result.put("affectedUsers", tokenService.countActiveUsers());
        return ResponseEntity.ok(Map.of("success", true, "rawData", result));
    }

    /**
     * 管理员单独调整某用户的配额。
     */
    @PutMapping("/api/v1/admin/token/quota/{userId}")
    public ResponseEntity<?> setUserQuotaForFrontend(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body
    ) {
        Long newLimit = parseLong(body.get("daily_token_limit"));
        if (newLimit == null) {
            return badRequest("daily_token_limit is required and must be a number");
        }
        if (newLimit < 1_000) {
            return badRequest("Token limit must be >= 1000");
        }

        tokenService.setUserQuota(userId, newLimit);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User quota updated"
        ));
    }

    // ==================== 聊天场景 ====================

    /**
     * 发起聊天前检测预计用量是否会超过配额。
     */
    @PostMapping({"/v1/chat/check", "/api/v1/chat/check"})
    public ResponseEntity<?> checkChat(@RequestBody Map<String, Object> body) {
        Long userId = parseUserId(body.get("userId"));
        if (userId == null) {
            return badRequest("userId is required");
        }

        String model = body.get("model") == null
                ? "default"
                : String.valueOf(body.get("model"));
        long estimatedTokens = parseLongOrDefault(body.get("estimatedTokens"), 2_000L);

        boolean allowed = tokenService.checkQuota(userId, estimatedTokens);
        UserTokenStatus status = tokenService.getDailyStatus(userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "allowed", allowed,
                "model", model,
                "quota", status.quota(),
                "used", status.used(),
                "remaining", status.remaining(),
                "overQuota", status.overQuota()
        ));
    }

    /**
     * 记录一次聊天实际产生的 Token 消耗。
     */
    @PostMapping({"/v1/chat/usage", "/api/v1/chat/usage"})
    public ResponseEntity<?> recordChat(@RequestBody Map<String, Object> body) {
        Long userId = parseUserId(body.get("userId"));
        if (userId == null) {
            return badRequest("userId is required");
        }

        String model = body.get("model") == null
                ? "default"
                : String.valueOf(body.get("model"));
        long promptTokens = parseLongOrDefault(body.get("promptTokens"), 0L);
        long completionTokens = parseLongOrDefault(body.get("completionTokens"), 0L);

        try {
            tokenService.recordUsage(userId, model, promptTokens, completionTokens);
        } catch (IllegalArgumentException exception) {
            return badRequest(exception.getMessage());
        }

        UserTokenStatus status = tokenService.getDailyStatus(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", status));
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", message));
    }

    private Long parseUserId(Object value) {
        return parseLong(value);
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private long parseLongOrDefault(Object value, long defaultValue) {
        Long parsed = parseLong(value);
        return parsed == null ? defaultValue : parsed;
    }

    private String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? "Token service request failed"
                : message;
    }
}
