package org.microsoft.qintelipass.dtos;

import lombok.Builder;
import lombok.Data;
import org.microsoft.qintelipass.entity.UserFreezeLog;
import org.microsoft.qintelipass.enums.UserFreezeAction;

import java.time.LocalDateTime;

@Data
@Builder
public class UserFreezeLogDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String department;
    private Long operatorId;
    private String operatorName;
    private UserFreezeAction action;
    private String reason;
    private Long censorAlertId;
    private Long previousTokenLimit;
    private String notificationMessage;
    private LocalDateTime operatedAt;

    public static UserFreezeLogDTO fromEntity(UserFreezeLog log) {
        if (log == null) {
            return null;
        }
        Long userId = null;
        if (log.getUser() != null) {
            userId = log.getUser().getId();
        }
        return UserFreezeLogDTO.builder()
                .id(log.getId())
                .userId(userId)
                .userName(log.getUserName())
                .department(log.getDepartment())
                .operatorId(log.getOperatorId())
                .operatorName(log.getOperatorName())
                .action(log.getAction())
                .reason(log.getReason())
                .censorAlertId(log.getCensorAlertId())
                .previousTokenLimit(log.getPreviousTokenLimit())
                .notificationMessage(log.getNotificationMessage())
                .operatedAt(log.getOperatedAt())
                .build();
    }
}