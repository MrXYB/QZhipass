package org.microsoft.qintelipass.dtos.request;

import lombok.Data;

@Data
public class CreateAgentRequest {
    private String name;
    private String prompt;
}
