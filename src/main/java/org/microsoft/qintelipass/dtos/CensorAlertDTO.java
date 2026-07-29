package org.microsoft.qintelipass.dtos;

import org.microsoft.qintelipass.enums.CensorAlertStatus;
import org.microsoft.qintelipass.models.CensorAlert;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public record CensorAlertDTO(
        Long id,
        String employeeId,
        String name,
        String department,
        String position,
        String triggeredAt,
        String ruleName,
        int periodDays,
        int threshold,
        long triggerCount,
        long currentCount,
        List<String> keywords,
        String status,
        String noticeSentAt,
        String email,
        String handledAt,
        String handledBy,
        List<AlertContextDTO> contexts
) {
    public static CensorAlertDTO from(CensorAlert alert) {
        return new CensorAlertDTO(
                alert.getId(),
                alert.getEmployeeId(),
                alert.getName(),
                alert.getDepartment(),
                alert.getPosition(),
                alert.getTriggeredAt() == null ? null : alert.getTriggeredAt().toString(),
                alert.getRuleName(),
                alert.getPeriodDays(),
                alert.getThreshold(),
                alert.getTriggerCount(),
                alert.getCurrentCount(),
                split(alert.getKeywords()),
                alert.getStatus() == CensorAlertStatus.HANDLED ? "handled" : "pending",
                alert.getNoticeSentAt() == null ? null : alert.getNoticeSentAt().toString(),
                alert.getEmail(),
                alert.getHandledAt() == null ? null : alert.getHandledAt().toString(),
                alert.getHandledBy(),
                split(alert.getContexts()).stream()
                        .map(AlertContextDTO::fromCompactString)
                        .toList()
        );
    }

    private static List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    public record AlertContextDTO(
            String time,
            String keyword,
            String before,
            String hit,
            String after
    ) {
        static AlertContextDTO fromCompactString(String value) {
            String[] parts = value.split("\\|", -1);
            String time = parts.length > 0 ? parts[0] : "";
            String keyword = parts.length > 1 ? decode(parts[1]) : "";
            String before = parts.length > 2 ? decode(parts[2]) : "";
            String hit = parts.length > 3 ? decode(parts[3]) : keyword;
            String after = parts.length > 4 ? decode(parts[4]) : "";
            return new AlertContextDTO(time, keyword, before, hit, after);
        }

        private static String decode(String value) {
            if (value == null || value.isBlank()) {
                return "";
            }
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        }
    }
}
