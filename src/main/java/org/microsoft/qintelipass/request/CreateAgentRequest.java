package org.microsoft.qintelipass.request;

import lombok.Data;

@Data
public class CreateAgentRequest {
    private String name;
    private String prompt;
}
