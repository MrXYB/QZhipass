package org.microsoft.qintelipass;

import org.microsoft.qintelipass.entity.User;
import org.microsoft.qintelipass.dtos.request.RegisterRequest;

public interface IRegisterable {
    User register(RegisterRequest request, String password);
}
