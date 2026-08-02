package org.microsoft.qintelipass.services;

import org.microsoft.qintelipass.dtos.UserFreezeLogDTO;

import java.util.List;

public interface UserFreezeService {
    UserFreezeLogDTO freezeUser(Long userId, String reason, Long censorAlertId, Long operatorId, String operatorName);

    UserFreezeLogDTO unfreezeUser(Long userId, String reason, Long operatorId, String operatorName);

    List<UserFreezeLogDTO> getFreezeLogs(Long userId);
}