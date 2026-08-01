package org.microsoft.qintelipass.services;

import org.jspecify.annotations.Nullable;

public interface ISmsService {
    @Nullable String sendSmsCode(String phoneNumber);
}
