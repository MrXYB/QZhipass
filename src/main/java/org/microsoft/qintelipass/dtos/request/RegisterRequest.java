package org.microsoft.qintelipass.dtos.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String name;
    private String password;
    private String phone;
    private String department;
}
