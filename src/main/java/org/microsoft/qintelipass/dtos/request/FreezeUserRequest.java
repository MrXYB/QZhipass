package org.microsoft.qintelipass.dtos.request;

import lombok.Data;

@Data
public class FreezeUserRequest {
    private String reason;
    private Long censorAlertId;
}